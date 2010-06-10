/**
 * Copyright 2009-2010 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

import com.threecrickets.prudence.util.MiniConnectionPoolManager;

/**
 * A SQL-backed cache. Automatically uses a {@link MiniConnectionPoolManager}
 * for data sources that support the {@link ConnectionPoolDataSource} interface.
 * <p>
 * This instance maintains a pool of read/write locks to guarantee atomicity of
 * storing, fetching and invalidating. It does not use SQL transactions. This
 * allows you disable transaction features in your database for better
 * performance. However, it also means that you should not have more than one
 * instance of this class working on the same set of keys, because they will not
 * be sharing the locks.
 * <p>
 * Also note that {@link #prune()} does not clean up unused locks. Since most
 * applications reuse cache keys anyway, this seems like an insignificant
 * "memory leak" cost in order to vastly improve pruning performance.
 * 
 * @author Tal Liron
 * @param <D>
 */
public class SqlCache<D extends DataSource> implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction with a max entry count of 1000 entries and 10 connections in
	 * the pool.
	 * 
	 * @param dataSource
	 *        The data source
	 */
	public SqlCache( D dataSource )
	{
		this( dataSource, 1000, 10 );
	}

	/**
	 * Construction.
	 * 
	 * @param dataSource
	 *        The data source
	 * @param maxSize
	 *        The max entry count
	 * @param poolSize
	 *        The number of connections in the pool
	 */
	public SqlCache( D dataSource, int maxSize, int poolSize )
	{
		this.dataSource = dataSource;
		this.maxSize = maxSize;

		if( dataSource instanceof ConnectionPoolDataSource )
			connectionPool = new MiniConnectionPoolManager( (ConnectionPoolDataSource) dataSource, poolSize );
		else
			connectionPool = null;
	}

	//
	// Attributes
	//

	/**
	 * The data source.
	 * 
	 * @return The data source
	 */
	public D getDataSource()
	{
		return dataSource;
	}

	/**
	 * The current max entry count.
	 * 
	 * @return Max entry count
	 * @see #setMaxSize(int)
	 */
	public int getMaxSize()
	{
		return maxSize;
	}

	/**
	 * @param maxSize
	 *        Max entry count
	 * @see #getMaxSize()
	 */
	public void setMaxSize( int maxSize )
	{
		this.maxSize = maxSize;
	}

	//
	// Operations
	//

	/**
	 * Makes sure that the required tables exist.
	 * 
	 * @param fresh
	 *        Whether to drop the table first
	 */
	public void validateTables( boolean fresh )
	{
		try
		{
			Connection connection = getConnection();

			try
			{
				Statement statement = connection.createStatement();
				try
				{
					if( fresh )
					{
						statement.execute( "DROP TABLE IF EXISTS " + entryTableName );
						statement.execute( "DROP TABLE IF EXISTS " + tagTableName );
					}

					statement.execute( "CREATE TABLE IF NOT EXISTS " + entryTableName
						+ " (key VARCHAR(255) PRIMARY KEY, string TEXT, media_type VARCHAR(255), language VARCHAR(255), character_set VARCHAR(255), expiration_date TIMESTAMP)" );
					statement.execute( "CREATE TABLE IF NOT EXISTS " + tagTableName + " (key VARCHAR(255), tag VARCHAR(255), FOREIGN KEY(key) REFERENCES " + entryTableName + "(key) ON DELETE CASCADE)" );
					statement.execute( "CREATE INDEX IF NOT EXISTS " + tagTableName + "_tag_idx ON " + tagTableName + " (tag)" );
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				connection.close();
			}
		}
		catch( SQLException x )
		{
			if( debug )
				x.printStackTrace();
		}
	}

	//
	// Cache
	//

	public void store( String key, Iterable<String> tags, CacheEntry entry )
	{
		if( debug )
			System.out.println( "Store: " + key + " " + tags );

		Lock lock = getLock( key ).writeLock();
		lock.lock();
		try
		{
			Connection connection = getConnection();

			try
			{
				boolean tryInsert = true;

				// Try updating this key

				String sql = "UPDATE " + entryTableName + " SET string=?, media_type=?, language=?, character_set=?, expiration_date=? WHERE key=?";
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setString( 1, entry.getString() );
					statement.setString( 2, entry.getMediaType() != null ? entry.getMediaType().getName() : null );
					statement.setString( 3, entry.getLanguage() != null ? entry.getLanguage().getName() : null );
					statement.setString( 4, entry.getCharacterSet() != null ? entry.getCharacterSet().getName() : null );
					statement.setTimestamp( 5, new Timestamp( entry.getExpirationDate().getTime() ) );
					statement.setString( 6, key );
					if( !statement.execute() && statement.getUpdateCount() > 0 )
					{
						if( debug )
							System.out.println( "Updated " + key );

						// Update worked, so no need to try insertion

						tryInsert = false;
					}
				}
				finally
				{
					statement.close();
				}

				if( tryInsert )
				{
					// Try inserting this key

					// But first make sure we have room...

					int size = countEntries( connection );
					if( size >= maxSize )
					{
						prune();

						size = countEntries( connection );
						if( size >= maxSize )
						{
							if( debug )
								System.out.println( "No room in cache (" + size + ", " + maxSize + ")" );

							return;
						}
					}

					// delete( connection, key );

					sql = "INSERT INTO " + entryTableName + " (key, string, media_type, language, character_set, expiration_date) VALUES (?, ?, ?, ?, ?, ?)";
					statement = connection.prepareStatement( sql );
					try
					{
						statement.setString( 1, key );
						statement.setString( 2, entry.getString() );
						statement.setString( 3, entry.getMediaType() != null ? entry.getMediaType().getName() : null );
						statement.setString( 4, entry.getLanguage() != null ? entry.getLanguage().getName() : null );
						statement.setString( 5, entry.getCharacterSet() != null ? entry.getCharacterSet().getName() : null );
						statement.setTimestamp( 6, new Timestamp( entry.getExpirationDate().getTime() ) );
						statement.execute();
					}
					finally
					{
						statement.close();
					}
				}

				// Clean out existing tags for this key

				sql = "DELETE FROM " + tagTableName + " WHERE key=?";
				statement = connection.prepareStatement( sql );
				try
				{
					statement.setString( 1, key );
					statement.execute();
				}
				finally
				{
					statement.close();
				}

				// Add tags for this key

				if( tags.iterator().hasNext() )
				{
					sql = "INSERT INTO " + tagTableName + " (key, tag) VALUES (?, ?)";
					statement = connection.prepareStatement( sql );
					statement.setString( 1, key );
					try
					{
						for( String tag : tags )
						{
							statement.setString( 2, tag );
							statement.execute();
						}
					}
					finally
					{
						statement.close();
					}
				}
			}
			finally
			{
				connection.close();
			}
		}
		catch( SQLException x )
		{
			if( debug )
				x.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}
	}

	public CacheEntry fetch( String key )
	{
		Lock lock = getLock( key ).readLock();
		lock.lock();
		try
		{
			Connection connection = getConnection();
			try
			{
				String sql = "SELECT string, media_type, language, character_set, expiration_date FROM " + entryTableName + " WHERE key=?";
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setString( 1, key );
					ResultSet rs = statement.executeQuery();
					try
					{
						if( rs.next() )
						{
							String string = rs.getString( 1 );
							MediaType mediaType = MediaType.valueOf( rs.getString( 2 ) );
							Language language = Language.valueOf( rs.getString( 3 ) );
							CharacterSet characterSet = CharacterSet.valueOf( rs.getString( 4 ) );
							Timestamp expirationDate = rs.getTimestamp( 5 );

							if( debug )
								System.out.println( "Fetched: " + key );

							CacheEntry entry = new CacheEntry( string, mediaType, language, characterSet, expirationDate );

							if( new java.util.Date().after( entry.getExpirationDate() ) )
							{
								lock.unlock();
								try
								{
									if( debug )
										System.out.println( "Stale entry: " + key );

									delete( connection, key );

									// (Note that this also discarded our lock,
									// but we kept it as a local variable)
								}
								finally
								{
									lock.lock();
								}
								return null;
							}

							return entry;
						}
					}
					finally
					{
						rs.close();
					}
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				connection.close();
			}
		}
		catch( SQLException x )
		{
			if( debug )
				x.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}

		if( debug )
			System.out.println( "Did not fetch: " + key );
		return null;
	}

	public void invalidate( String tag )
	{
		try
		{
			Connection connection = getConnection();
			try
			{
				List<String> tagged = getTagged( connection, tag );
				if( tagged.isEmpty() )
					return;

				ArrayList<Lock> locks = new ArrayList<Lock>( tagged.size() );

				String sql = "DELETE FROM " + entryTableName + " WHERE key IN (";
				for( String key : tagged )
				{
					sql += "?,";
					locks.add( getLock( key ).writeLock() );
				}
				sql = sql.substring( 0, sql.length() - 1 ) + ")";

				for( Lock lock : locks )
					lock.lock();
				try
				{
					PreparedStatement statement = connection.prepareStatement( sql );
					try
					{
						int i = 1;
						for( String key : tagged )
							statement.setString( i++, key );
						if( !statement.execute() )
						{
							if( debug )
								System.out.println( "Invalidated " + statement.getUpdateCount() );
						}
					}
					finally
					{
						statement.close();
					}

					for( String key : tagged )
						discardLock( key );
				}
				finally
				{
					for( Lock lock : locks )
						lock.unlock();
				}
			}
			finally
			{
				connection.close();
			}
		}
		catch( SQLException x )
		{
			if( debug )
				x.printStackTrace();
		}
	}

	public void prune()
	{
		// Note that this will not discard locks

		try
		{
			Connection connection = getConnection();
			try
			{
				String sql = "DELETE FROM " + entryTableName + " WHERE expiration_date<?";
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setTimestamp( 1, new Timestamp( System.currentTimeMillis() ) );
					if( !statement.execute() )
					{
						if( debug )
							System.out.println( "Pruned " + statement.getUpdateCount() );
					}
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				connection.close();
			}
		}
		catch( SQLException x )
		{
			if( debug )
				x.printStackTrace();
		}
	}

	public void reset()
	{
		// This is not atomic, but does it matter?

		validateTables( true );
		locks.clear();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Whether to print debug messages to standard out.
	 */
	protected volatile boolean debug = false;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The data source.
	 */
	private final D dataSource;

	/**
	 * The data source.
	 */
	private final MiniConnectionPoolManager connectionPool;

	/**
	 * The entry table name.
	 */
	private final String entryTableName = "prudence_cache";

	/**
	 * The tag table name.
	 */
	private final String tagTableName = "prudence_cache_tag";

	/**
	 * The current max cache size.
	 */
	private volatile int maxSize;

	/**
	 * A pool of read/write locks per key.
	 */
	private ConcurrentMap<String, ReadWriteLock> locks = new ConcurrentHashMap<String, ReadWriteLock>();

	/**
	 * Gets the connection, either from the connection pool or directly from the
	 * data source.
	 * 
	 * @return The connection
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException
	{
		if( connectionPool != null )
			return connectionPool.getConnection();
		else
			return dataSource.getConnection();
	}

	/**
	 * Count all entries.
	 * 
	 * @param connection
	 *        The connection
	 * @return The entry count
	 * @throws SQLException
	 */
	private int countEntries( Connection connection ) throws SQLException
	{
		Statement statement = connection.createStatement();
		try
		{
			String sql = "SELECT COUNT(key) FROM " + entryTableName;
			ResultSet rs = statement.executeQuery( sql );
			try
			{
				if( rs.next() )
					return rs.getInt( 1 );
			}
			finally
			{
				rs.close();
			}
		}
		finally
		{
			statement.close();
		}

		return -1;
	}

	/**
	 * Delete an entry.
	 * 
	 * @param connection
	 *        The connection
	 * @param key
	 *        The key
	 * @throws SQLException
	 */
	private void delete( Connection connection, String key ) throws SQLException
	{
		Lock lock = getLock( key ).writeLock();
		lock.lock();
		try
		{
			String sql = "DELETE FROM " + entryTableName + " WHERE key=?";
			PreparedStatement statement = connection.prepareStatement( sql );
			try
			{
				statement.setString( 1, key );
				if( !statement.execute() )
				{
					if( debug && statement.getUpdateCount() > 0 )
						System.out.println( "Deleted: " + key );
				}
			}
			finally
			{
				statement.close();
			}
		}
		finally
		{
			lock.unlock();
			discardLock( key );
		}
	}

	/**
	 * Gets a list of tagged keys.
	 * 
	 * @param connection
	 *        The connection
	 * @param tag
	 *        The tag
	 * @return The list of tagged keys
	 * @throws SQLException
	 */
	private List<String> getTagged( Connection connection, String tag ) throws SQLException
	{
		ArrayList<String> tagged = new ArrayList<String>();
		String sql = "SELECT key FROM " + tagTableName + " WHERE tag=?";
		PreparedStatement statement = connection.prepareStatement( sql );
		try
		{
			statement.setString( 1, tag );
			ResultSet rs = statement.executeQuery();
			try
			{
				while( rs.next() )
					tagged.add( rs.getString( 1 ) );
			}
			finally
			{
				rs.close();
			}
		}
		finally
		{
			statement.close();
		}

		return tagged;
	}

	/**
	 * Gets a unique lock for a key.
	 * 
	 * @param key
	 *        The key
	 * @return The lock
	 */
	private ReadWriteLock getLock( String key )
	{
		ReadWriteLock lock = locks.get( key );
		if( lock == null )
		{
			lock = new ReentrantReadWriteLock();
			ReadWriteLock existing = locks.putIfAbsent( key, lock );
			if( existing != null )
				lock = existing;
		}
		return lock;
	}

	/**
	 * Discards the lock for a key.
	 * 
	 * @param key
	 *        The key
	 */
	private void discardLock( String key )
	{
		locks.remove( key );
	}
}
