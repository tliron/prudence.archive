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
 * This may be unnecessary in future versions of Restlet. See <a
 * href="http://restlet.tigris.org/issues/show_bug.cgi?id=1056">issue 1056</a>.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 */
public class NormalizingRedirector extends Redirector
{
	//
	// Construction
	//

	/**
	 * Construction for {@link Redirector#MODE_SERVER_OUTBOUND}.
	 * 
	 * @param context
	 *        The context
	 * @param targetTemplate
	 *        The target template
	 */
	public NormalizingRedirector( Context context, String targetTemplate )
	{
		this( context, targetTemplate, MODE_SERVER_OUTBOUND );
	}

	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param targetPattern
	 *        The target template
	 * @param mode
	 *        The redirection mode
	 */
	public NormalizingRedirector( Context context, String targetPattern, int mode )
	{
		super( context, targetPattern, mode );
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "NormalizingRedirector" );
		setDescription( "Redirector that normalizes relative paths" );
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

		// Relative references should be considered relative to the resource
		// reference
		if( reference.getBaseRef() == null )
			reference.setBaseRef( request.getResourceRef() );

		return reference.getTargetRef();
	}
}
