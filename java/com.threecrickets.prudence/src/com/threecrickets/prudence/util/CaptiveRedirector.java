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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.routing.Redirector;

/**
 * A {@link Redirector} that normalizes relative paths.
 * <p>
 * This may be unecessary in future versions of Restlet. See <a
 * href="http://restlet.tigris.org/issues/show_bug.cgi?id=1056">issue 1056</a>.
 * 
 * @author Tal Liron
 */
public class CaptiveRedirector extends Redirector
{
	//
	// Constants
	//

	public static final String CAPTIVE_REFERENCE = "com.threecrickets.prudence.util.CaptiveRedirector.captiveReference";

	//
	// Static attributes
	//

	public static Reference getCaptiveReference( Request request )
	{
		return (Reference) request.getAttributes().get( CAPTIVE_REFERENCE );
	}

	public static void setCaptiveReference( Request request, Reference captiveReference )
	{
		request.getAttributes().put( CAPTIVE_REFERENCE, captiveReference );
	}

	//
	// Construction
	//

	/**
	 * @param context
	 * @param targetTemplate
	 */
	public CaptiveRedirector( Context context, String targetTemplate )
	{
		super( context, targetTemplate );
	}

	/**
	 * @param context
	 * @param targetPattern
	 * @param mode
	 */
	public CaptiveRedirector( Context context, String targetPattern, int mode )
	{
		super( context, targetPattern, mode );
	}

	//
	// Redirector
	//

	@Override
	public void handle( Request request, Response response )
	{
		setCaptiveReference( request, request.getResourceRef() );
		super.handle( request, response );
	}
}
