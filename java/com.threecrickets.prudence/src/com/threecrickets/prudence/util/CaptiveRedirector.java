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

import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Redirector;

/**
 * A {@link Redirector} that keeps track of the captured reference.
 * 
 * @author Tal Liron
 */
public class CaptiveRedirector extends ResolvingRedirector
{
	//
	// Constants
	//

	/**
	 * Request attribute of the captive {@link Reference}.
	 * 
	 * @see #getCaptiveReference(Request)
	 * @see #setCaptiveReference(Request, Reference)
	 */
	public static final String CAPTIVE_REFERENCE = "com.threecrickets.prudence.util.CaptiveRedirector.captiveReference";

	//
	// Static attributes
	//

	/**
	 * The captive reference.
	 * 
	 * @param request
	 *        The request
	 * @return The captured reference
	 * @see #setCaptiveReference(Request, Reference)
	 */
	public static Reference getCaptiveReference( Request request )
	{
		return (Reference) request.getAttributes().get( CAPTIVE_REFERENCE );
	}

	/**
	 * The captive reference.
	 * 
	 * @param request
	 *        The request
	 * @param captiveReference
	 *        The captive reference
	 * @see #getCaptiveReference(Request)
	 */
	public static void setCaptiveReference( Request request, Reference captiveReference )
	{
		request.getAttributes().put( CAPTIVE_REFERENCE, captiveReference );
	}

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
	 * @param hostRoot
	 *        Whether to set the base reference to the host root URI
	 */
	public CaptiveRedirector( Context context, String targetTemplate, boolean hostRoot )
	{
		this( context, targetTemplate, hostRoot, MODE_SERVER_OUTBOUND );
	}

	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param targetPattern
	 *        The target pattern
	 * @param mode
	 *        The redirection mode
	 * @param hostRoot
	 *        Whether to set the base reference to the host root URI
	 */
	public CaptiveRedirector( Context context, String targetPattern, boolean hostRoot, int mode )
	{
		super( context, targetPattern, mode );
		this.hostRoot = hostRoot;
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "CaptiveRedirector" );
		setDescription( "Redirector that keeps track of the captive reference" );
	}

	//
	// Redirector
	//

	@Override
	public void handle( Request request, Response response )
	{
		Reference rootRef = hostRoot ? null : request.getRootRef();
		if( rootRef == null )
			// The root reference could be null (e.g. in RIAP)
			rootRef = new Reference( request.getResourceRef().getHostIdentifier() + "/" );

		setCaptiveReference( request, new Reference( rootRef, request.getResourceRef() ) );

		// Avoid endless loops
		if( targetTemplate.equals( request.getResourceRef().getPath() ) )
		{
			response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
			return;
		}

		super.handle( request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected Reference getTargetRef( Request request, Response response )
	{
		// Cache target reference
		String attribute = TARGET_REFERENCE + response.hashCode();
		Map<String, Object> attributes = request.getAttributes();
		Reference targetRef = (Reference) attributes.get( attribute );
		if( targetRef == null )
		{
			targetRef = super.getTargetRef( request, response );
			attributes.put( attribute, targetRef );
		}
		return targetRef;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Request attribute of the target reference.
	 */
	private static final String TARGET_REFERENCE = "com.threecrickets.prudence.util.CaptiveRedirector.targetReference.";

	/**
	 * Whether to set the base reference to the host root URI.
	 */
	private final boolean hostRoot;
}
