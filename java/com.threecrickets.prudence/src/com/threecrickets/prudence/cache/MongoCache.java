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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import org.bson.types.Binary;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

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
public class MongoCache implements Cache
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
	public MongoCache() throws UnknownHostException, MongoException
	{
		this( new Mongo() );
	}

	/**
	 * Construction for database "prudence", collection "cache".
	 * 
	 * @param mongo
	 *        The MongoDB connection
	 */
	public MongoCache( Mongo mongo )
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
	public MongoCache( Mongo mongo, String dbName )
	{
		this( mongo.getDB( dbName ), "cache" );
	}

	/**
	 * Construction.
	 * 
	 * @param mongo
	 *        The MongoDB connection
	 * @param dbName
	 *        The MongoDB database name
	 * @param collectionName
	 *        The name of the collection to use for the cache
	 */
	public MongoCache( Mongo mongo, String dbName, String collectionName )
	{
		this( mongo.getDB( dbName ), collectionName );
	}

	/**
	 * Construction.
	 * 
	 * @param db
	 *        The MongoDB database
	 * @param collectionName
	 *        The name of the collection to use for the cache
	 */
	public MongoCache( DB db, String collectionName )
	{
		cacheCollection = db.getCollection( collectionName );
	}

	/**
	 * Construction.
	 * 
	 * @param collection
	 */
	public MongoCache( DBCollection collection )
	{
		this.cacheCollection = collection;
		collection.ensureIndex( TAG_INDEX );
		collection.ensureIndex( EXPIRATION_DATE_INDEX );
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

	public void store( String key, Iterable<String> tags, CacheEntry entry )
	{
		if( debug )
			System.out.println( "Store: " + key + " " + tags );

		DBObject query = new BasicDBObject();
		query.put( "_id", key );

		DBObject document = new BasicDBObject();
		DBObject set = new BasicDBObject();
		document.put( "$set", set );

		// Note: In binary mode, the expirationDate is also inside the binary
		// dump, however we need it here, too, to allow for fast pruning.
		set.put( "expirationDate", entry.getExpirationDate() );

		if( ( tags != null ) && tags.iterator().hasNext() )
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
				if( debug )
					x.printStackTrace();
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
		}

		// Upsert
		cacheCollection.update( query, document, true, false );
	}

	public CacheEntry fetch( String key )
	{
		DBObject query = new BasicDBObject();
		query.put( "_id", key );
		DBObject document = cacheCollection.findOne( query );
		if( document != null )
		{
			Date expirationDate = (Date) document.get( "expirationDate" );
			if( expirationDate.before( new Date() ) )
			{
				cacheCollection.remove( query );

				if( debug )
					System.out.println( "Stale entry: " + key );

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

					if( string != null )
						cacheEntry = new CacheEntry( string, mediaType, language, characterSet, encoding, documentModificationDate, expirationDate );
					else
						cacheEntry = new CacheEntry( bytes, mediaType, language, characterSet, encoding, documentModificationDate, expirationDate );
				}

				if( debug )
					System.out.println( "Fetched: " + key );

				return cacheEntry;
			}
			catch( IOException x )
			{
				if( debug )
					x.printStackTrace();
			}
			catch( ClassNotFoundException x )
			{
				if( debug )
					x.printStackTrace();
			}
		}
		else
		{
			if( debug )
				System.out.println( "Did not fetch: " + key );
		}

		return null;
	}

	public void invalidate( String tag )
	{
		DBObject query = new BasicDBObject();
		query.put( "tags", tag );
		cacheCollection.remove( query );
	}

	public void prune()
	{
		DBObject query = new BasicDBObject();
		DBObject lt = new BasicDBObject();
		query.put( "$lt", lt );

		lt.put( "expirationDate", new Date() );
		cacheCollection.remove( query );
	}

	public void reset()
	{
		cacheCollection.remove( new BasicDBObject() );
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
		TAG_INDEX.put( "expirationDate", 1 );
	}

	/**
	 * Binary type.
	 */
	private static final byte BINARY_TYPE = 0;

	/**
	 * The MongoDB collection used for the cache.
	 */
	private final DBCollection cacheCollection;

	/**
	 * Whether to print debug messages to standard out.
	 */
	private volatile boolean debug = false;

	/**
	 * Whether to store entries by serializing them into BSON binaries.
	 */
	private volatile boolean isBinary = false;
}
