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

package com.threecrickets.prudence.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * A PHP-style $_REQUEST map.
 * <p>
 * See PHP's <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a>.
 * 
 * @author Tal Liron
 */
public class LazyInitializationExposedRequest extends LazyInitializationMap<String, String>
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param exposedGet
	 *        The exposed get map to use
	 * @param exposedPost
	 *        The exposed post map to use
	 * @param exposedCookie
	 *        The exposed cookie map to use
	 */
	public LazyInitializationExposedRequest( Map<String, String> exposedGet, Map<String, String> exposedPost, Map<String, String> exposedCookie )
	{
		super( new HashMap<String, String>() );
		this.exposedGet = exposedGet;
		this.exposedPost = exposedPost;
		this.exposedCookie = exposedCookie;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// LazyInitializationMap
	//

	@Override
	protected void initialize()
	{
		map.putAll( exposedGet );
		map.putAll( exposedPost );
		map.putAll( exposedCookie );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The exposed get map to use.
	 */
	private final Map<String, String> exposedGet;

	/**
	 * The exposed post map to use.
	 */
	private final Map<String, String> exposedPost;

	/**
	 * The exposed cookie map to use.
	 */
	private final Map<String, String> exposedCookie;
}