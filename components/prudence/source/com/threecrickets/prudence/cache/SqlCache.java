/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.cache;

import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;

/**
 * A SQL-backed cache. Internally uses <a
 * href="http://commons.apache.org/dbcp/">Apache Commons DBCP</a> for connection
 * pooling.
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
 */
public class SqlCache implements Cache
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
	public SqlCache( DataSource dataSource )
	{
		this( dataSource, 1000, 10 );
	}

	/**
	 * Constructor.
	 * 
	 * @param dataSource
	 *        The data source
	 * @param maxSize
	 *        The max entry count
	 * @param poolSize
	 *        The number of connections in the pool
	 */
	public SqlCache( DataSource dataSource, int maxSize, int poolSize )
	{
		this.maxSize = maxSize;

		GenericObjectPool connectionPool = new GenericObjectPool( null, poolSize );
		new PoolableConnectionFactory( new DataSourceConnectionFactory( dataSource ), connectionPool, null, null, false, true );
		this.dataSource = new PoolingDataSource( connectionPool );
	}

	//
	// Attributes
	//

	/**
	 * The data source.
	 * 
	 * @return The data source
	 */
	public DataSource getDataSource()
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
			Connection connection = connect();
			if( connection == null )
				return;

			try
			{
				Statement statement = connection.createStatement();
				try
				{
					if( fresh )
					{
						statement.execute( "DROP TABLE IF EXISTS " + cacheTableName );
						statement.execute( "DROP TABLE IF EXISTS " + cacheTagsTableName );
					}

					statement
						.execute( "CREATE TABLE IF NOT EXISTS "
							+ cacheTableName
							+ " (key VARCHAR(255) PRIMARY KEY, data BLOB, media_type VARCHAR(255), language VARCHAR(255), character_set VARCHAR(255), encoding VARCHAR(255), headers TEXT, document_modification_date TIMESTAMP, expiration_date TIMESTAMP)" );
					statement.execute( "CREATE TABLE IF NOT EXISTS " + cacheTagsTableName + " (key VARCHAR(255), tag VARCHAR(255), FOREIGN KEY(key) REFERENCES " + cacheTableName + "(key) ON DELETE CASCADE)" );
					statement.execute( "CREATE INDEX IF NOT EXISTS " + cacheTagsTableName + "_tag_idx ON " + cacheTagsTableName + " (tag)" );
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
			logger.log( Level.WARNING, "Could not validate that tables exist", x );
		}
	}

	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
		logger.fine( "Store: " + key );

		Lock lock = getLock( key ).writeLock();
		lock.lock();
		try
		{
			Connection connection = connect();
			if( connection == null )
				return;

			try
			{
				boolean tryInsert = true;

				// Try updating this key

				String sql = "UPDATE " + cacheTableName + " SET data=?, media_type=?, language=?, character_set=?, encoding=?, headers=?, document_modification_date=?, expiration_date=? WHERE key=?";
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setBytes( 1, entry.getString() != null ? entry.getString().getBytes() : entry.getBytes() );
					statement.setString( 2, entry.getMediaType() != null ? entry.getMediaType().getName() : null );
					statement.setString( 3, entry.getLanguage() != null ? entry.getLanguage().getName() : null );
					statement.setString( 4, entry.getCharacterSet() != null ? entry.getCharacterSet().getName() : null );
					statement.setString( 5, entry.getEncoding() != null ? entry.getEncoding().getName() : null );
					statement.setString( 6, entry.getHeaders() == null ? "" : entry.getHeaders().getQueryString() );
					statement.setTimestamp( 7, new Timestamp( entry.getDocumentModificationDate().getTime() ) );
					statement.setTimestamp( 8, new Timestamp( entry.getExpirationDate().getTime() ) );
					statement.setString( 9, key );
					if( !statement.execute() && statement.getUpdateCount() > 0 )
					{
						logger.fine( "Updated " + key );

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
							logger.fine( "No room in cache (" + size + ", " + maxSize + ")" );
							return;
						}
					}

					// delete( connection, key );

					sql = "INSERT INTO " + cacheTableName + " (key, data, media_type, language, character_set, encoding, headers, document_modification_date, expiration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
					statement = connection.prepareStatement( sql );
					try
					{
						statement.setString( 1, key );
						statement.setBytes( 2, entry.getString() != null ? entry.getString().getBytes() : entry.getBytes() );
						statement.setString( 3, getName( entry.getMediaType() ) );
						statement.setString( 4, getName( entry.getLanguage() ) );
						statement.setString( 5, getName( entry.getCharacterSet() ) );
						statement.setString( 6, getName( entry.getEncoding() ) );
						statement.setString( 7, entry.getHeaders() == null ? "" : entry.getHeaders().getQueryString() );
						statement.setTimestamp( 8, new Timestamp( entry.getDocumentModificationDate().getTime() ) );
						statement.setTimestamp( 9, new Timestamp( entry.getExpirationDate().getTime() ) );
						statement.execute();
					}
					finally
					{
						statement.close();
					}
				}

				// Clean out existing tags for this key

				sql = "DELETE FROM " + cacheTagsTableName + " WHERE key=?";
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

				String[] tags = entry.getTags();
				if( ( tags != null ) && ( tags.length > 0 ) )
				{
					sql = "INSERT INTO " + cacheTagsTableName + " (key, tag) VALUES (?, ?)";
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
			logger.log( Level.WARNING, "Could not store cache entry", x );
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
			Connection connection = connect();
			if( connection == null )
				return null;

			try
			{
				String sql = "SELECT data, media_type, language, character_set, encoding, headers, document_modification_date, expiration_date FROM " + cacheTableName + " WHERE key=?";
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setString( 1, key );
					ResultSet rs = statement.executeQuery();
					try
					{
						if( rs.next() )
						{
							byte[] data = rs.getBytes( 1 );
							MediaType mediaType = MediaType.valueOf( rs.getString( 2 ) );
							Language language = Language.valueOf( rs.getString( 3 ) );
							CharacterSet characterSet = CharacterSet.valueOf( rs.getString( 4 ) );
							Encoding encoding = Encoding.valueOf( rs.getString( 5 ) );
							String rawHeaders = rs.getString( 6 );
							Form headers = ( rawHeaders != null ) && ( rawHeaders.length() > 0 ) ? new Form( rawHeaders ) : null;
							Timestamp documentModificationDate = rs.getTimestamp( 7 );
							Timestamp expirationDate = rs.getTimestamp( 8 );

							logger.fine( "Fetched: " + key );

							CacheEntry entry;
							if( encoding != null )
								entry = new CacheEntry( data, mediaType, language, characterSet, encoding, headers, documentModificationDate, expirationDate );
							else
							{
								try
								{
									entry = new CacheEntry( new String( data ), mediaType, language, characterSet, null, headers, documentModificationDate, expirationDate );
								}
								catch( IOException x )
								{
									throw new RuntimeException( "Should never happen if data is not encoded!" );
								}
							}

							if( new java.util.Date().after( entry.getExpirationDate() ) )
							{
								lock.unlock();
								try
								{
									logger.fine( "Stale entry: " + key );
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
			logger.log( Level.WARNING, "Could not fetch cache entry", x );
		}
		finally
		{
			lock.unlock();
		}

		logger.fine( "Did not fetch: " + key );
		return null;
	}

	public void invalidate( String tag )
	{
		try
		{
			Connection connection = connect();
			if( connection == null )
				return;

			try
			{
				List<String> tagged = getTagged( connection, tag );
				if( tagged.isEmpty() )
					return;

				ArrayList<Lock> locks = new ArrayList<Lock>( tagged.size() );

				String sql = "DELETE FROM " + cacheTableName + " WHERE key IN (";
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
							logger.fine( "Invalidated " + statement.getUpdateCount() );
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
			logger.log( Level.WARNING, "Could not invalidate cache tag", x );
		}
	}

	public void prune()
	{
		// Note that this will not discard locks

		try
		{
			Connection connection = connect();
			if( connection == null )
				return;

			try
			{
				String sql = "DELETE FROM " + cacheTableName + " WHERE expiration_date<?";
				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					statement.setTimestamp( 1, new Timestamp( System.currentTimeMillis() ) );
					if( !statement.execute() )
						logger.fine( "Pruned " + statement.getUpdateCount() );
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
			logger.log( Level.WARNING, "Could not prune", x );
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
	 * Call when server is up.
	 */
	protected void up()
	{
		if( up.compareAndSet( false, true ) )
			logger.info( "Up! " + dataSource );
	}

	/**
	 * Call when server is down.
	 */
	protected void down()
	{
		if( up.compareAndSet( true, false ) )
			logger.severe( "Down! " + dataSource );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The logger.
	 */
	private final Logger logger = Logger.getLogger( this.getClass().getCanonicalName() );

	/**
	 * The data source.
	 */
	private final DataSource dataSource;

	/**
	 * The entry table name.
	 */
	private final String cacheTableName = "prudence_cache";

	/**
	 * The tag table name.
	 */
	private final String cacheTagsTableName = "prudence_cache_tags";

	/**
	 * The current max cache size.
	 */
	private volatile int maxSize;

	/**
	 * A pool of read/write locks per key.
	 */
	private ConcurrentMap<String, ReadWriteLock> locks = new ConcurrentHashMap<String, ReadWriteLock>();

	/**
	 * Whether the server has last been seen as up.
	 */
	private AtomicBoolean up = new AtomicBoolean();

	/**
	 * Connect to data source.
	 * 
	 * @return The connection or null if failed
	 */
	private Connection connect()
	{
		try
		{
			Connection connection = dataSource.getConnection();
			up();
			return connection;
		}
		catch( SQLException x )
		{
			down();
			return null;
		}
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
			String sql = "SELECT COUNT(key) FROM " + cacheTableName;
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
			String sql = "DELETE FROM " + cacheTableName + " WHERE key=?";
			PreparedStatement statement = connection.prepareStatement( sql );
			try
			{
				statement.setString( 1, key );
				if( !statement.execute() )
				{
					if( logger.isLoggable( Level.FINE ) && statement.getUpdateCount() > 0 )
						logger.fine( "Deleted: " + key );
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
		String sql = "SELECT key FROM " + cacheTagsTableName + " WHERE tag=?";
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

	/**
	 * Metadata name.
	 * 
	 * @param metadata
	 *        The metadata or null
	 * @return The name or null
	 */
	private static String getName( Metadata metadata )
	{
		return metadata == null ? null : metadata.getName();
	}
}
