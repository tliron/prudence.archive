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
public class NormalizingRedirector extends Redirector
{
	//
	// Construction
	//

	/**
	 * @param context
	 * @param targetTemplate
	 */
	public NormalizingRedirector( Context context, String targetTemplate )
	{
		super( context, targetTemplate );
	}

	/**
	 * @param context
	 * @param targetPattern
	 * @param mode
	 */
	public NormalizingRedirector( Context context, String targetPattern, int mode )
	{
		super( context, targetPattern, mode );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Redirector
	//

	@Override
	protected Reference getTargetRef( Request request, Response response )
	{
		Reference reference = super.getTargetRef( request, response );
		// reference.setBaseRef( request.getResourceRef().getBaseRef() );
		return reference.getTargetRef();
	}
}
