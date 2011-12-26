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

import com.threecrickets.prudence.GeneratedTextResource;

/**
 * Stores nothing. Useful if you want to disable server-side caching but still
 * want client-side caching on for {@link GeneratedTextResource}.
 * 
 * @author Tal Liron
 */
public class FakeCache implements Cache
{
	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
	}

	public CacheEntry fetch( String key )
	{
		return null;
	}

	public void invalidate( String tag )
	{
	}

	public void prune()
	{
	}

	public void reset()
	{
	}
}
