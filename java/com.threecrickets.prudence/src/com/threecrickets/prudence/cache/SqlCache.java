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

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

import com.threecrickets.prudence.util.MiniConnectionPoolManager;

/**
 * A SQL-backed cache. Automatically uses a {@link MiniConnectionPoolManager}
 * for data sources that support the {@link ConnectionPoolDataSource} interface.
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
	 * Construction with a max entry count of 1000 entries.
	 * 
	 * @param dataSource
	 *        The data source
	 */
	public SqlCache( D dataSource )
	{
		this( dataSource, 1000 );
	}

	/**
	 * Construction.
	 * 
	 * @param dataSource
	 *        The data source
	 * @param maxSize
	 *        The max entry count
	 */
	public SqlCache( D dataSource, int maxSize )
	{
		this.dataSource = dataSource;
		this.maxSize = maxSize;

		if( dataSource instanceof ConnectionPoolDataSource )
			connectionPool = new MiniConnectionPoolManager( (ConnectionPoolDataSource) dataSource, 10 );
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
						statement.execute( "DROP TABLE IF EXISTS " + groupTableName );
					}

					statement.execute( "CREATE TABLE IF NOT EXISTS " + entryTableName
						+ " (key VARCHAR(255) PRIMARY KEY, string TEXT, media_type VARCHAR(255), language VARCHAR(255), character_set VARCHAR(255), expiration_date TIMESTAMP)" );
					statement.execute( "CREATE TABLE IF NOT EXISTS " + groupTableName + " (key VARCHAR(255), group_key VARCHAR(255), FOREIGN KEY(key) REFERENCES " + entryTableName + "(key) ON DELETE CASCADE)" );
					statement.execute( "CREATE INDEX IF NOT EXISTS " + groupTableName + "_group_key_idx ON " + groupTableName + " (group_key)" );
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

	public void store( String key, Iterable<String> groupKeys, CacheEntry entry )
	{
		if( debug )
			System.out.println( "Store: " + key + " " + groupKeys );

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

				// Clean out existing group entries for this key

				sql = "DELETE FROM " + groupTableName + " WHERE key=?";
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

				// Add group entries for this key

				if( groupKeys.iterator().hasNext() )
				{
					sql = "INSERT INTO " + groupTableName + " (key, group_key) VALUES (?, ?)";
					statement = connection.prepareStatement( sql );
					statement.setString( 1, key );
					try
					{
						for( String groupKey : groupKeys )
						{
							statement.setString( 2, groupKey );
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
	}

	public CacheEntry fetch( String key )
	{
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
								delete( connection, key );
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

		if( debug )
			System.out.println( "Did not fetch: " + key );
		return null;
	}

	public void invalidate( String groupKey )
	{
		try
		{
			Connection connection = getConnection();
			try
			{
				List<String> group = getGroup( connection, groupKey );
				if( group.isEmpty() )
					return;

				String sql = "DELETE FROM " + entryTableName + " WHERE key IN (";
				for( int i = group.size(); i > 0; i-- )
					sql += "?,";
				sql = sql.substring( 0, sql.length() - 1 ) + ")";

				PreparedStatement statement = connection.prepareStatement( sql );
				try
				{
					int i = 1;
					for( String key : group )
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
	 * The group table name.
	 */
	private final String groupTableName = "prudence_cache_group";

	/**
	 * The current max cache size.
	 */
	private volatile int maxSize;

	private Connection getConnection() throws SQLException
	{
		if( connectionPool != null )
			return connectionPool.getConnection();
		else
			return dataSource.getConnection();
	}

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

	public void delete( Connection connection, String key ) throws SQLException
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

	public List<String> getGroup( Connection connection, String groupKey ) throws SQLException
	{
		ArrayList<String> group = new ArrayList<String>();
		String sql = "SELECT key FROM " + groupTableName + " WHERE group_key=?";
		PreparedStatement statement = connection.prepareStatement( sql );
		try
		{
			statement.setString( 1, groupKey );
			ResultSet rs = statement.executeQuery();
			try
			{
				while( rs.next() )
					group.add( rs.getString( 1 ) );
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

		return group;
	}
}
