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
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.BSONObject;
import org.bson.types.Binary;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * A <a href="http://www.mongodb.org/">MongoDB</a>-backed cache.
 * <p>
 * Uses a dedicated MongoDB collection to store the cache, creating it if it
 * doesn't exist.
 * <p>
 * Supports storing entries as either binary dumps or detailed documents. Binary
 * dumps take less space and are slightly more efficient, while detailed
 * documents are far easier to debug. Binary mode is off by default.
 * <p>
 * Note that MongoDB's indexing facility allows for very high performance
 * invalidation and pruning.
 * 
 * @author Tal Liron
 */
public class MongoDbCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction for localhost connection, database "prudence", collection
	 * "cache".
	 * 
	 * @throws MongoException
	 * @throws UnknownHostException
	 */
	public MongoDbCache() throws UnknownHostException, MongoException
	{
		this( new Mongo() );
	}

	/**
	 * Construction for database "prudence", collection "cache".
	 * 
	 * @param mongo
	 *        The MongoDB connection
	 */
	public MongoDbCache( Mongo mongo )
	{
		this( mongo, "prudence" );
	}

	/**
	 * Construction for collection "cache".
	 * 
	 * @param mongo
	 *        The MongoDB connection
	 * @param dbName
	 *        The MongoDB database name
	 */
	public MongoDbCache( Mongo mongo, String dbName )
	{
		this( mongo.getDB( dbName ), "cache" );
	}

	/**
	 * Constructor.
	 * 
	 * @param mongo
	 *        The MongoDB connection
	 * @param dbName
	 *        The MongoDB database name
	 * @param collectionName
	 *        The name of the collection to use for the cache
	 */
	public MongoDbCache( Mongo mongo, String dbName, String collectionName )
	{
		this( mongo.getDB( dbName ), collectionName );
	}

	/**
	 * Constructor.
	 * 
	 * @param db
	 *        The MongoDB database
	 * @param collectionName
	 *        The name of the collection to use for the cache
	 */
	public MongoDbCache( DB db, String collectionName )
	{
		this( db.getCollection( collectionName ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param collection
	 */
	public MongoDbCache( DBCollection collection )
	{
		this.cacheCollection = collection;
		try
		{
			collection.ensureIndex( TAG_INDEX );
			collection.ensureIndex( EXPIRATION_DATE_INDEX );
			up();
		}
		catch( com.mongodb.MongoException.Network x )
		{
			down();
		}
	}

	//
	// Attributes
	//

	/**
	 * Whether to store entries by serializing them into BSON binaries.
	 * 
	 * @return A boolean
	 * @see #setBinary
	 */
	public boolean isBinary()
	{
		return isBinary;
	}

	/**
	 * @param isBinary
	 *        A boolean
	 * @see #isBinary
	 */
	public void setBinary( boolean isBinary )
	{
		this.isBinary = isBinary;
	}

	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
		logger.fine( "Store: " + key );

		DBObject query = new BasicDBObject();
		query.put( "_id", key );

		DBObject document = new BasicDBObject();
		DBObject set = new BasicDBObject();
		document.put( "$set", set );

		// Note: In binary mode, the expirationDate is also inside the binary
		// dump, however we need it here, too, to allow for fast pruning.
		set.put( "expirationDate", entry.getExpirationDate() );

		String[] tags = entry.getTags();
		if( ( tags != null ) && ( tags.length > 0 ) )
			set.put( "tags", tags );

		if( isBinary )
		{
			try
			{
				Binary binary = new Binary( BINARY_TYPE, entry.toBytes() );
				set.put( "binary", binary );
			}
			catch( IOException x )
			{
				logger.log( Level.WARNING, "Could not serialize binary", x );
			}
		}
		else
		{
			Date documentModificationDate = entry.getDocumentModificationDate();
			if( documentModificationDate != null )
				set.put( "documentModificationDate", documentModificationDate );

			String string = entry.getString();
			if( string != null )
				set.put( "string", string );

			byte[] bytes = entry.getBytes();
			if( bytes != null )
			{
				Binary binary = new Binary( BINARY_TYPE, bytes );
				set.put( "bytes", binary );
			}

			MediaType mediaType = entry.getMediaType();
			if( mediaType != null )
				set.put( "mediaType", mediaType.getName() );

			Language language = entry.getLanguage();
			if( language != null )
				set.put( "language", language.getName() );

			Encoding encoding = entry.getEncoding();
			if( encoding != null )
				set.put( "encoding", encoding.getName() );

			CharacterSet characterSet = entry.getCharacterSet();
			if( characterSet != null )
				set.put( "characterSet", characterSet.getName() );

			Form headers = entry.getHeaders();
			if( headers != null )
			{
				BasicDBList list = new BasicDBList();
				for( Parameter header : headers )
				{
					BasicDBObject object = new BasicDBObject();
					object.put( "name", header.getName() );
					object.put( "value", header.getValue() );
					list.add( object );
				}
				set.put( "headers", list );
			}
		}

		// Upsert
		try
		{
			cacheCollection.update( query, document, true, false );
			up();
		}
		catch( com.mongodb.MongoException.Network x )
		{
			down();
		}
	}

	public CacheEntry fetch( String key )
	{
		DBObject query = new BasicDBObject();
		query.put( "_id", key );
		try
		{
			DBObject document = cacheCollection.findOne( query );
			up();
			if( document != null )
			{
				Date expirationDate = (Date) document.get( "expirationDate" );
				if( expirationDate.before( new Date() ) )
				{
					cacheCollection.remove( query );
					logger.fine( "Stale entry: " + key );
					return null;
				}

				try
				{
					CacheEntry cacheEntry = null;

					byte[] bytes = (byte[]) document.get( "binary" );
					if( bytes != null )
					{
						return new CacheEntry( bytes );
					}
					else
					{
						Date documentModificationDate = (Date) document.get( "documentModificationDate" );
						String string = (String) document.get( "string" );
						bytes = (byte[]) document.get( "bytes" );
						MediaType mediaType = MediaType.valueOf( (String) document.get( "mediaType" ) );
						Language language = Language.valueOf( (String) document.get( "language" ) );
						Encoding encoding = Encoding.valueOf( (String) document.get( "encoding" ) );
						CharacterSet characterSet = CharacterSet.valueOf( (String) document.get( "characterSet" ) );

						Form headers = null;
						Object storedHeaders = document.get( "headers" );
						if( storedHeaders instanceof Collection )
						{
							headers = new Form();
							for( Object storedHeader : (Collection<?>) storedHeaders )
							{
								if( storedHeader instanceof BSONObject )
								{
									BSONObject storedHeaderBson = (BSONObject) storedHeader;
									Object name = storedHeaderBson.get( "name" );
									Object value = storedHeaderBson.get( "value" );
									if( ( name != null ) && ( value != null ) )
										headers.add( name.toString(), value.toString() );
								}
							}
						}

						if( string != null )
							cacheEntry = new CacheEntry( string, mediaType, language, characterSet, encoding, headers, documentModificationDate, expirationDate );
						else
							cacheEntry = new CacheEntry( bytes, mediaType, language, characterSet, encoding, headers, documentModificationDate, expirationDate );
					}

					logger.fine( "Fetched: " + key );
					return cacheEntry;
				}
				catch( IOException x )
				{
					logger.log( Level.WARNING, "Could not deserialize cache entry", x );
				}
				catch( ClassNotFoundException x )
				{
					logger.log( Level.WARNING, "Could not deserialize cache entry", x );
				}
			}
			else
				logger.fine( "Did not fetch: " + key );
		}
		catch( com.mongodb.MongoException.Network x )
		{
			down();
		}

		return null;
	}

	public void invalidate( String tag )
	{
		DBObject query = new BasicDBObject();
		query.put( "tags", tag );

		try
		{
			cacheCollection.remove( query );
			up();
		}
		catch( com.mongodb.MongoException.Network x )
		{
			down();
		}
	}

	public void prune()
	{
		DBObject query = new BasicDBObject();
		DBObject lt = new BasicDBObject();
		query.put( "$lt", lt );

		lt.put( "expirationDate", new Date() );
		try
		{
			cacheCollection.remove( query );
		}
		catch( com.mongodb.MongoException.Network x )
		{
			down();
		}
	}

	public void reset()
	{
		try
		{
			cacheCollection.remove( new BasicDBObject() );
			up();
		}
		catch( com.mongodb.MongoException.Network x )
		{
			down();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Options for ensuring the tag index on the cache collection.
	 */
	private static final DBObject TAG_INDEX = new BasicDBObject();

	/**
	 * Options for ensuring the expiration date index on the cache collection.
	 */
	private static final DBObject EXPIRATION_DATE_INDEX = new BasicDBObject();

	static
	{
		TAG_INDEX.put( "tags", 1 );
		EXPIRATION_DATE_INDEX.put( "expirationDate", 1 );
	}

	/**
	 * Binary type.
	 */
	private static final byte BINARY_TYPE = 0;

	/**
	 * The logger.
	 */
	private final Logger logger = Logger.getLogger( this.getClass().getCanonicalName() );

	/**
	 * The MongoDB collection used for the cache.
	 */
	private final DBCollection cacheCollection;

	/**
	 * Whether to store entries by serializing them into BSON binaries.
	 */
	private volatile boolean isBinary = false;

	/**
	 * Whether MongoDB has last been seen as up.
	 */
	private AtomicBoolean up = new AtomicBoolean();

	/**
	 * Call when MongoDB is up.
	 */
	private void up()
	{
		if( up.compareAndSet( false, true ) )
			logger.info( "Up! " + cacheCollection.getDB().getMongo() );
	}

	/**
	 * Call when MongoDB is down.
	 */
	private void down()
	{
		if( up.compareAndSet( true, false ) )
			logger.severe( "Down! " + cacheCollection.getDB().getMongo() );
	}
}
