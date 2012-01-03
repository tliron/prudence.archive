/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.cache;

import com.threecrickets.prudence.GeneratedTextResource;

/**
 * An interface for cache backends to be used with {@link GeneratedTextResource}
 * .
 * <p>
 * Ideas for places to store cache entries: in-process memory, the filesystem,
 * databases, and dedicated network services.
 * <p>
 * Note: implementations must be thread-safe.
 * 
 * @author Tal Liron
 * @see CacheEntry
 */
public interface Cache
{
	/**
	 * Stores an entry in the cache. The entry is guaranteed to be un-fetchable
	 * after {@link CacheEntry#getExpirationDate()}, although it may very well
	 * be un-fetchable sooner.
	 * <p>
	 * Keys are unique to the cache. Storing for a key that already exists will
	 * replace the entry if it exists.
	 * 
	 * @param key
	 *        A key unique to the cache
	 * @param entry
	 *        The entry
	 */
	public void store( String key, CacheEntry entry );

	/**
	 * Fetches an entry from the cache if it's there and has not yet expired.
	 * 
	 * @param key
	 *        A key unique to the cache
	 * @return An entry or null
	 */
	public CacheEntry fetch( String key );

	/**
	 * Makes sure that all tagged entries are un-fetchable.
	 * 
	 * @param tag
	 */
	public void invalidate( String tag );

	/**
	 * Removes expired cache entries.
	 */
	public void prune();

	/**
	 * Removes all entries in the cache.
	 */
	public void reset();
}
