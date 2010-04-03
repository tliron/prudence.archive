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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tal Liron
 */
public class InProcessCache implements Cache
{
	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
		cache.put( key, entry );
	}

	public CacheEntry fetch( String key )
	{
		CacheEntry entry = cache.get( key );
		if( entry != null )
		{
			if( new Date().after( entry.getExpirationDate() ) )
				cache.remove( key );
			else
				return entry;
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();

}
