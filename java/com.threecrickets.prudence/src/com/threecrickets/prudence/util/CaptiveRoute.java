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

package com.threecrickets.prudence.util;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

/**
 * A {@link Route} that always scores 0.0 for captured requests. Useful for
 * making sure that captured requests are captured again.
 * 
 * @author Tal Liron
 * @see CaptiveRouter
 */
@SuppressWarnings("deprecation")
public class CaptiveRoute extends Route
{
	//
	// Construction
	//

	public CaptiveRoute( Restlet next )
	{
		super( next );
	}

	public CaptiveRoute( Router router, String uriTemplate, Restlet next )
	{
		super( router, uriTemplate, next );
	}

	public CaptiveRoute( Router router, Template template, Restlet next )
	{
		super( router, template, next );
	}

	//
	// Route
	//

	@Override
	public float score( Request request, Response response )
	{
		if( CaptiveRedirector.getCaptiveReference( request ) != null )
			return 0f;

		return super.score( request, response );
	}
}
