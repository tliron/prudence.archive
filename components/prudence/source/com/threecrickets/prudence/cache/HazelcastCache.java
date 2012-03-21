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

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

/**
 * A <a href="http://www.hazelcast.com/">Hazelcast</a>-backed cache.
 * <p>
 * Uses a Hazelcast map and a Hazelcast multimap, defaulting to the names
 * "com.threecrickets.prudence.prudence.cache" and
 * "com.threecrickets.prudence.prduence.cacheTags" respectively. Refer to
 * Hazelcast documentation for instructions on how to configure them.
 * 
 * @author Tal Liron
 */
public class HazelcastCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public HazelcastCache()
	{
		this( null );
	}

	/**
	 * Constructor.
	 * 
	 * @param hazelcast
	 *        The hazelcast instance or null to use the default instance
	 */
	public HazelcastCache( HazelcastInstance hazelcast )
	{
		this( hazelcast, "com.threecrickets.prudence.cache", "com.threecrickets.prudence.cacheTags" );
	}

	/**
	 * Constructor.
	 * 
	 * @param hazelcast
	 *        The Hazelcast instance or null to use the default instance
	 * @param cacheName
	 *        The Hazelcast map name for the cache
	 */
	public HazelcastCache( HazelcastInstance hazelcast, String cacheName, String tagMapName )
	{
		this.cacheName = cacheName;
		this.cacheTagsName = tagMapName;
		this.hazelcast = hazelcast != null ? hazelcast : Hazelcast.getDefaultInstance();
	}

	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
		logger.fine( "Store: " + key );

		ConcurrentMap<String, CacheEntry> cache = getCache();
		cache.put( key, entry );

		String[] tags = entry.getTags();
		if( ( tags != null ) && ( tags.length > 0 ) )
		{
			MultiMap<String, String> tagMap = getTagMap();
			for( String tag : tags )
				tagMap.put( tag, key );
		}
	}

	public CacheEntry fetch( String key )
	{
		ConcurrentMap<String, CacheEntry> cache = getCache();
		CacheEntry entry = cache.get( key );
		if( entry != null )
		{
			if( new Date().after( entry.getExpirationDate() ) )
			{
				logger.fine( "Stale entry: " + key );
				cache.remove( key );
				entry = null;
			}
			else
				logger.fine( "Fetched: " + key );
		}
		else
			logger.fine( "Did not fetch: " + key );

		return entry;
	}

	public void invalidate( String tag )
	{
		MultiMap<String, String> tagMap = getTagMap();
		Collection<String> tagged = tagMap.remove( tag );
		if( tagged != null )
		{
			ConcurrentMap<String, CacheEntry> cache = getCache();
			for( String key : tagged )
			{
				logger.fine( "Invalidate " + tag + ": " + key );
				cache.remove( key );
			}
		}
	}

	public void prune()
	{
		// This is too expensive to do in Hazelcast (we'll have to traverse all
		// cached entries) and not worth it.
	}

	public void reset()
	{
		// This is not atomic, but does it matter?

		getCache().clear();
		getTagMap().clear();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The logger.
	 */
	private final Logger logger = Logger.getLogger( this.getClass().getCanonicalName() );

	/**
	 * The Hazelcast instance.
	 */
	private final HazelcastInstance hazelcast;

	/**
	 * The Hazelcast map name for the cache.
	 */
	private final String cacheName;

	/**
	 * The Hazelcast map name for the tag map.
	 */
	private final String cacheTagsName;

	/**
	 * The cache.
	 * 
	 * @return The cache
	 */
	private ConcurrentMap<String, CacheEntry> getCache()
	{
		return hazelcast.getMap( cacheName );
	}

	/**
	 * The tag map.
	 * 
	 * @return The tag map
	 */
	private MultiMap<String, String> getTagMap()
	{
		return hazelcast.getMultiMap( cacheTagsName );
	}
}
