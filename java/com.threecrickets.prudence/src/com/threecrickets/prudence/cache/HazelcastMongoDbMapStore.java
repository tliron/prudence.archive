/**
 * Copyright 2009-2011 Three Crickets LLC.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.Binary;
import org.restlet.Component;

import com.hazelcast.core.MapStore;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.prudence.util.IoUtil;

/**
 * A Hazelcast persistence implementation over <a
 * href="http://www.mongodb.org/">MongoDB</a>.
 * <p>
 * The MongoDB connection must be stored as "mongoDb.defaultConnection" in the
 * {@link Component}'s context.
 * <p>
 * The MongoDB database will be "prudence".
 * 
 * @author Tal Liron
 * @param <K>
 *        Key
 * @param <V>
 *        Value
 */
public abstract class HazelcastMongoDbMapStore<K, V> implements MapStore<K, V>
{
	//
	// Constants
	//

	/**
	 * MongoDB default connection attribute for a {@link Component}.
	 */
	public static final String MONGODB_DEFAULT_CONNECTION_ATTRIBUTE = "mongoDb.defaultConnection";

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param collectionName
	 *        The name of the MongoDB collection used for the store
	 */
	public HazelcastMongoDbMapStore( String collectionName )
	{
		this.collectionName = collectionName;
	}

	//
	// Attributes
	//

	/**
	 * The MongoDB collection used for the store.
	 * 
	 * @return The MongoDB collection
	 */
	public DBCollection getCollection()
	{
		if( collection == null )
		{
			Component component = InstanceUtil.getComponent();
			if( component != null )
			{
				Mongo mongo = (Mongo) component.getContext().getAttributes().get( MONGODB_DEFAULT_CONNECTION_ATTRIBUTE );
				if( mongo != null )
				{
					DB db = (DB) mongo.getDB( "prudence" );
					if( db != null )
						collection = db.getCollection( collectionName );
				}
			}
		}

		if( collection == null )
			throw new RuntimeException( "MongoDB connection must be configured in order to use HazelcastMongoDbMapStore" );

		return collection;
	}

	//
	// MapStore
	//

	@SuppressWarnings("unchecked")
	public V load( K key )
	{
		DBCollection collection = getCollection();
		DBObject query = new BasicDBObject();

		query.put( "_id", key );

		DBObject value = collection.findOne( query );
		if( value != null )
			return (V) fromBinary( (byte[]) value.get( "value" ) );
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<K, V> loadAll( Collection<K> keys )
	{
		DBCollection collection = getCollection();
		DBObject query = new BasicDBObject();
		DBObject in = new BasicDBObject();
		BasicDBList keysList = new BasicDBList();

		query.put( "_id", in );
		in.put( "$in", keysList );
		keysList.addAll( keys );

		HashMap<K, V> map = new HashMap<K, V>();
		for( DBCursor cursor = collection.find( query ); cursor.hasNext(); )
		{
			DBObject value = cursor.next();
			map.put( (K) value.get( "_id" ), (V) fromBinary( (byte[]) value.get( "value" ) ) );
		}

		return map;
	}

	public void store( K key, V value )
	{
		DBCollection collection = getCollection();
		DBObject query = new BasicDBObject();
		DBObject update = new BasicDBObject();
		DBObject set = new BasicDBObject();

		query.put( "_id", key );
		update.put( "$set", set );
		set.put( "value", toBinary( value ) );

		collection.update( query, update, true, false );
	}

	public void storeAll( Map<K, V> map )
	{
		for( Map.Entry<K, V> entry : map.entrySet() )
			store( entry.getKey(), entry.getValue() );
	}

	public void delete( K key )
	{
		DBCollection collection = getCollection();
		DBObject query = new BasicDBObject();

		query.put( "_id", key );

		collection.remove( query );
	}

	public void deleteAll( Collection<K> keys )
	{
		DBCollection collection = getCollection();
		DBObject query = new BasicDBObject();
		DBObject in = new BasicDBObject();
		BasicDBList keysList = new BasicDBList();

		query.put( "_id", in );
		in.put( "$in", keysList );
		keysList.addAll( keys );

		collection.remove( query );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Binary type.
	 */
	private static final byte BINARY_TYPE = 0;

	/**
	 * The name of the MongoDB collection used for the store.
	 */
	private final String collectionName;

	/**
	 * The MongoDB collection used for the store.
	 */
	private volatile DBCollection collection;

	/**
	 * Serialize an object into a BSON binary.
	 * 
	 * @param o
	 *        The object
	 * @return The binary
	 */
	private static Binary toBinary( Object o )
	{
		try
		{
			return new Binary( BINARY_TYPE, IoUtil.serialize( o ) );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
	}

	/**
	 * Deserialize an object from a BSON binary.
	 * 
	 * @param <V>
	 * @param binary
	 *        The binary or null
	 * @return The object or null
	 */
	private static Object fromBinary( byte[] binary )
	{
		if( binary == null )
			return null;

		try
		{
			return IoUtil.deserialize( binary );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
		catch( ClassNotFoundException x )
		{
			throw new RuntimeException( x );
		}
	}
}
