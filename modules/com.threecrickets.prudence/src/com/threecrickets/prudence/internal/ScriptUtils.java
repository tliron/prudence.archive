/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.internal;

import org.restlet.data.Request;

/**
 * Utility methods for Prudence.
 * 
 * @author Tal Liron
 */
public abstract class ScriptUtils
{
	/**
	 * Retrieves that part of the resource reference between the base reference
	 * and the optional attributes.
	 * 
	 * @param request
	 *        The request or null
	 * @param def
	 *        A default value to return if the request is null or if the
	 *        relative part of the reference is empty
	 * @return The relative part of the reference or the default value
	 */
	public static String getRelativePart( Request request, String def )
	{
		String url = request.getResourceRef().getRemainingPart( true );

		if( url != null )
		{
			int query = url.indexOf( '?' );
			if( query != -1 )
				url = url.substring( 0, query );
		}

		if( ( url == null ) || ( url.length() == 0 ) || url.equals( "/" ) )
			return def;
		else
			return url;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Private constructor.
	 */
	private ScriptUtils()
	{
	}
}
