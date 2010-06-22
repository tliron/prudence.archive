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
public class LazyInitializationRequest extends LazyInitializationMap<String, String>
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param getMap
	 *        The get map to use
	 * @param postMap
	 *        The post map to use
	 * @param cookieMap
	 *        The cookie map to use
	 */
	public LazyInitializationRequest( Map<String, String> getMap, Map<String, String> postMap, Map<String, String> cookieMap )
	{
		super( new HashMap<String, String>() );
		this.getMap = getMap;
		this.postMap = postMap;
		this.cookieMap = cookieMap;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// LazyInitializationMap
	//

	@Override
	protected void initialize()
	{
		map.putAll( getMap );
		map.putAll( postMap );
		map.putAll( cookieMap );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The get map to use.
	 */
	private final Map<String, String> getMap;

	/**
	 * The post map to use.
	 */
	private final Map<String, String> postMap;

	/**
	 * The cookie map to use.
	 */
	private final Map<String, String> cookieMap;
}