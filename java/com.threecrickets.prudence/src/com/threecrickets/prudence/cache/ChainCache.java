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

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Allows chaining of caches together. During fetch, caches are tested in order.
 * Other operations work on all caches.
 * 
 * @author Tal Liron
 */
public class ChainCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction.
	 */
	public ChainCache()
	{
	}

	/**
	 * Construction.
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

	//
	// Cache
	//

	public void store( String key, Iterable<String> groupKeys, CacheEntry entry )
	{
		for( Cache cache : caches )
			cache.store( key, groupKeys, entry );
	}

	public CacheEntry fetch( String key )
	{
		for( Cache cache : caches )
		{
			CacheEntry entry = cache.fetch( key );
			if( entry != null )
				return entry;
		}

		return null;
	}

	public void invalidate( String groupKey )
	{
		for( Cache cache : caches )
			cache.invalidate( groupKey );
	}

	public void prune()
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The chained caches.
	 */
	private CopyOnWriteArrayList<Cache> caches = new CopyOnWriteArrayList<Cache>();
}
