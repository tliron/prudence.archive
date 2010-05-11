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

import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;

/**
 * A PHP-style $_GET map.
 * <p>
 * See PHP's <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a>.
 * 
 * @author Tal Liron
 */
public class LazyInitializationExposedGet extends LazyInitializationMap<String, String>
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param map
	 *        The map to initialize
	 * @param request
	 *        The request
	 */
	public LazyInitializationExposedGet( Map<String, String> map, Request request )
	{
		super( map );
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
		if( request.getMethod().equals( Method.GET ) )
		{
			Form form = request.getResourceRef().getQueryAsForm();
			for( Parameter parameter : form )
				map.put( parameter.getName(), parameter.getValue() );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The request.
	 */
	private final Request request;
}