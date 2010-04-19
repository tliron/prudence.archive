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

package com.threecrickets.prudence.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.routing.Filter;

/**
 * A {@link Filter} that adds cache control directives to responses.
 * 
 * @author Tal Liron
 */
public class CacheControlFilter extends Filter
{
	//
	// Constants
	//

	/**
	 * A far future max age (10 years)
	 */
	public static int FAR_FUTURE = 10 * 365 * 24 * 60 * 60;

	//
	// Construction
	//

	/**
	 * Construction with a default max age of {@link #FAR_FUTURE}.
	 * 
	 * @param context
	 *        The context
	 */
	public CacheControlFilter( Context context )
	{
		this( context, null );
	}

	/**
	 * Construction with a default max age of {@link #FAR_FUTURE}.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 */
	public CacheControlFilter( Context context, Restlet next )
	{
		this( context, next, FAR_FUTURE );
	}

	/**
	 * Construction with specific default max age.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param defaultMaxAge
	 *        The default max age, in seconds, or -1 to signify "no-cache"
	 */
	public CacheControlFilter( Context context, Restlet next, int defaultMaxAge )
	{
		super( context, next );
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "CacheControlFilter" );
		setDescription( "A Filter that adds cache-control headers to responses" );
		this.defaultMaxAge = defaultMaxAge;
	}

	//
	// Attributes
	//

	/**
	 * @return A map of media types to its max age, in seconds, or -1 to signify
	 *         "no-cache"
	 */
	public Map<MediaType, Integer> getMaxAgeForMediaType()
	{
		return maxAgeForMediaType;
	}

	/**
	 * @return The default max age, in seconds, or -1 to signify "no-cache"
	 * @see #setDefaultMaxAge(int)
	 */
	public int getDefaultMaxAge()
	{
		return defaultMaxAge;
	}

	/**
	 * @param defaultMaxAge
	 *        The default max age, in seconds, or -1 to signify "no-cache"
	 * @see #getDefaultMaxAge()
	 */
	public void setDefaultMaxAge( int defaultMaxAge )
	{
		this.defaultMaxAge = defaultMaxAge;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Filter
	//

	@Override
	protected void afterHandle( Request request, Response response )
	{
		if( response.isEntityAvailable() )
		{
			MediaType mediaType = response.getEntity().getMediaType();
			Integer maxAge = maxAgeForMediaType.get( mediaType );
			// System.out.println( mediaType );

			if( maxAge == null )
				maxAge = defaultMaxAge;

			List<CacheDirective> cacheDirectives = response.getCacheDirectives();
			cacheDirectives.clear();
			if( maxAge == -1 )
				cacheDirectives.add( CacheDirective.noCache() );
			else
				cacheDirectives.add( CacheDirective.maxAge( maxAge ) );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<MediaType, Integer> maxAgeForMediaType = new HashMap<MediaType, Integer>();

	private int defaultMaxAge = -1;
}
