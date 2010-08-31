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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An in-process (heap) memory cache.
 * <p>
 * Note that this implementation does not check for overall heap consumption or
 * free system memory. Make sure you set the maximum size appropriate for your
 * system!
 * 
 * @author Tal Liron
 */
public class InProcessMemoryCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction of 1mb cache.
	 */
	public InProcessMemoryCache()
	{
		this( 1024 * 1024 * 1024 );
	}

	/**
	 * Construction.
	 * 
	 * @param maxSize
	 *        Initial max size in bytes
	 */
	public InProcessMemoryCache( long maxSize )
	{
		this.maxSize = maxSize;
	}

	//
	// Attributes
	//

	/**
	 * The current cache size.
	 * 
	 * @return Size in bytes
	 */
	public long getSize()
	{
		return size.get();
	}

	/**
	 * The current max cache size.
	 * 
	 * @return Max size in bytes
	 * @see #setMaxSize(long)
	 */
	public long getMaxSize()
	{
		return maxSize;
	}

	/**
	 * @param maxSize
	 *        Max size in bytes
	 * @see #getMaxSize()
	 */
	public void setMaxSize( long maxSize )
	{
		this.maxSize = maxSize;
	}

	//
	// Cache
	//

	public void store( String key, Iterable<String> tags, CacheEntry entry )
	{
		int entrySize = entry.getSize();

		if( debug )
			System.out.println( "Store: " + key + " " + tags );

		CacheEntry removed = cache.put( key, entry );
		if( removed != null )
			size.addAndGet( -removed.getString().length() );
		size.addAndGet( entrySize );

		if( size.get() > maxSize )
		{
			// Note: We are only attempting pruning once. However, under heavy
			// concurrency it might be possible that a subsequent attempt to
			// prune would succeed in making room for us. Is it worth retrying?
			// This issue should be given more thought. TODO.

			prune();

			if( size.get() > maxSize )
			{
				// No room for us :(
				removed = cache.remove( key );
				if( removed != null )
					size.addAndGet( -entrySize );

				if( debug )
					System.out.println( "No room in cache for " + entrySize + " (" + size.get() + ", " + maxSize + ")" );

				return;
			}
		}

		if( tags != null )
		{
			for( String tag : tags )
			{
				Set<String> tagged = tagMap.get( tag );
				if( tagged == null )
				{
					tagged = new CopyOnWriteArraySet<String>();
					Set<String> existing = tagMap.putIfAbsent( tag, tagged );
					if( existing != null )
						tagged = existing;
				}
				tagged.add( key );
			}
		}
	}

	public CacheEntry fetch( String key )
	{
		CacheEntry entry = cache.get( key );
		if( entry != null )
		{
			if( new Date().after( entry.getExpirationDate() ) )
			{
				if( debug )
					System.out.println( "Stale entry: " + key );

				entry = cache.remove( key );
				if( entry != null )
					size.addAndGet( -entry.getString().length() );
				else
					entry = null;
			}
			else
			{
				if( debug )
					System.out.println( "Fetched: " + key );
				return entry;
			}
		}

		if( debug )
			System.out.println( "Did not fetch: " + key );
		return null;
	}

	public void invalidate( String tag )
	{
		Set<String> tagged = tagMap.remove( tag );
		if( tagged != null )
		{
			for( String key : tagged )
			{
				if( debug )
					System.out.println( "Invalidate " + tag + ": " + key );

				CacheEntry removed = cache.remove( key );
				if( removed != null )
					size.addAndGet( -removed.getString().length() );
			}
		}
	}

	public void prune()
	{
		Date now = new Date();
		for( Map.Entry<String, CacheEntry> entry : cache.entrySet() )
		{
			if( now.after( entry.getValue().getExpirationDate() ) )
			{
				CacheEntry removed = cache.remove( entry.getKey() );
				if( removed != null )
				{
					if( debug )
						System.out.println( "Pruned " + entry.getKey() );

					size.addAndGet( -removed.getString().length() );
				}
			}
		}
	}

	public void reset()
	{
		// This is not atomic, but does it matter?

		cache.clear();
		tagMap.clear();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The cached entries.
	 */
	private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();

	/**
	 * The tagged keys, for invalidation.
	 */
	private final ConcurrentMap<String, Set<String>> tagMap = new ConcurrentHashMap<String, Set<String>>();

	/**
	 * The current cache size.
	 */
	private final AtomicLong size = new AtomicLong();

	/**
	 * The current max cache size.
	 */
	private volatile long maxSize;

	/**
	 * Whether to print debug messages to standard out.
	 */
	private volatile boolean debug = false;
}
