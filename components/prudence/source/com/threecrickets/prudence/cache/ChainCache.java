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

import java.util.Arrays;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Allows chaining of caches together in order, where the faster, less reliable
 * caches are assumed to be before the slower, more reliable caches.
 * <p>
 * During fetch, caches are tested in order. In backtrack mode (the default),
 * when a hit occurs, the entry is stored in all previous caches before the hit,
 * so that subsequent fetches would find the entry in the faster caches. Other
 * operations always work on all caches indiscriminately.
 * 
 * @author Tal Liron
 */
public class ChainCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public ChainCache()
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param caches
	 *        The initial caches
	 */
	public ChainCache( Cache... caches )
	{
		this.caches.addAll( Arrays.asList( caches ) );
	}

	//
	// Attributes
	//

	/**
	 * The chained caches.
	 * 
	 * @return The chained caches
	 */
	public CopyOnWriteArrayList<Cache> getCaches()
	{
		return caches;
	}

	/**
	 * When true (the default), makes sure to store a cache hit in previous
	 * caches along the chain.
	 * 
	 * @return The backtrack mode
	 */
	public boolean isBacktrack()
	{
		return backtrack;
	}

	/**
	 * @return The backtrack mode
	 * @see #isBacktrack()
	 */
	public boolean getBacktrack()
	{
		return isBacktrack();
	}

	/**
	 * @param backtrack
	 *        The backtrack mode
	 * @see #isBacktrack()
	 */
	public void setBacktrack( boolean backtrack )
	{
		this.backtrack = backtrack;
	}

	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
		for( Cache cache : caches )
			cache.store( key, entry );
	}

	public CacheEntry fetch( String key )
	{
		for( ListIterator<Cache> iterator = caches.listIterator(); iterator.hasNext(); )
		{
			Cache cache = iterator.next();
			CacheEntry entry = cache.fetch( key );
			if( entry != null )
			{
				if( backtrack )
				{
					// Store in previous caches
					if( iterator.hasPrevious() )
						iterator.previous();
					while( iterator.hasPrevious() )
					{
						cache = iterator.previous();
						cache.store( key, entry );
					}
				}

				return entry;
			}
		}

		return null;
	}

	public void invalidate( String tag )
	{
		for( Cache cache : caches )
			cache.invalidate( tag );
	}

	public void prune()
	{
		for( Cache cache : caches )
			cache.prune();
	}

	public void reset()
	{
		for( Cache cache : caches )
			cache.reset();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The chained caches.
	 */
	private CopyOnWriteArrayList<Cache> caches = new CopyOnWriteArrayList<Cache>();

	/**
	 * When true, makes sure to store a cache hit in previous caches along the
	 * chain.
	 */
	private volatile boolean backtrack = true;
}
