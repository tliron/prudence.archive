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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.Hazelcast;

/**
 * A <a href="http://www.hazelcast.com/">Hazelcast</a>-backed cache.
 * <p>
 * Uses two Hazelcast maps, defaulting to the names "prudence.cache" and
 * "prduence.tagMap". Refer to Hazelcast documentation for instructions on how
 * to configure them.
 * 
 * @author Tal Liron
 */
public class HazelcastCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction.
	 */
	public HazelcastCache()
	{
		this( "prudence.cache", "prudence.tagMap" );
	}

	/**
	 * Construction.
	 * 
	 * @param cacheName
	 *        The Hazelcast map name for the cache
	 * @param tagMapName
	 *        The Hazelcast map name for the tag map
	 */
	public HazelcastCache( String cacheName, String tagMapName )
	{
		this.cacheName = cacheName;
		this.tagMapName = tagMapName;
	}

	//
	// Cache
	//

	public void store( String key, Iterable<String> tags, CacheEntry entry )
	{
		if( debug )
			System.out.println( "Store: " + key + " " + tags );

		ConcurrentMap<String, CacheEntry> cache = getCache();
		cache.put( key, entry );

		if( tags != null )
		{
			ConcurrentMap<String, Set<String>> tagMap = getTagMap();
			for( String tag : tags )
			{
				Set<String> tagged = tagMap.get( tag );
				if( tagged == null )
					tagged = new HashSet<String>();
				tagged.add( key );
				tagMap.put( tag, tagged );
			}
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
				if( debug )
					System.out.println( "Stale entry: " + key );

				entry = cache.remove( key );
			}
			else
			{
				if( debug )
					System.out.println( "Fetched: " + key );
			}
		}
		else
		{
			if( debug )
				System.out.println( "Did not fetch: " + key );
		}
		return entry;

	}

	public void invalidate( String tag )
	{
		ConcurrentMap<String, Set<String>> tagMap = getTagMap();
		Set<String> tagged = tagMap.remove( tag );
		if( tagged != null )
		{
			ConcurrentMap<String, CacheEntry> cache = getCache();
			for( String key : tagged )
			{
				if( debug )
					System.out.println( "Invalidate " + tag + ": " + key );

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
	 * The Hazelcast map name for the cache.
	 */
	private final String cacheName;

	/**
	 * The Hazelcast map name for the tag map.
	 */
	private final String tagMapName;

	/**
	 * Whether to print debug messages to standard out.
	 */
	private volatile boolean debug = false;

	/**
	 * The cache.
	 * 
	 * @return The cache
	 */
	private ConcurrentMap<String, CacheEntry> getCache()
	{
		return Hazelcast.getMap( cacheName );
	}

	/**
	 * The tag map.
	 * 
	 * @return The tag map
	 */
	private ConcurrentMap<String, Set<String>> getTagMap()
	{
		return Hazelcast.getMap( tagMapName );
	}
}
