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

package com.threecrickets.prudence.internal.lazy;

import java.util.HashMap;

import org.restlet.Request;
import org.restlet.data.Cookie;

/**
 * A PHP-style $_COOKIE map.
 * <p>
 * See PHP's <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a>.
 * 
 * @author Tal Liron
 */
public class LazyInitializationCookie extends LazyInitializationMap<String, String>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        The request
	 */
	public LazyInitializationCookie( Request request )
	{
		super( new HashMap<String, String>() );
		this.request = request;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// LazyInitializationMap
	//

	@Override
	protected void initialize()
	{
		for( Cookie cookie : request.getCookies() )
			map.put( cookie.getName(), cookie.getValue() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The request.
	 */
	private final Request request;
}