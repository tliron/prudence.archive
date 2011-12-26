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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.routing.Redirector;

/**
 * A {@link Redirector} that keeps track of the captured reference.
 * 
 * @author Tal Liron
 * @see CapturingRouter
 */
public class CapturingRedirector extends ResolvingRedirector
{
	//
	// Constants
	//

	/**
	 * Request attribute of the captive {@link Reference} for a {@link Request}.
	 * 
	 * @see #getCapturedReference(Request)
	 * @see #setCapturedReference(Request, Reference)
	 */
	public static final String CAPTURED_REFERENCE = "com.threecrickets.prudence.util.CapturingRedirector.capturedReference";

	//
	// Static attributes
	//

	/**
	 * The captured reference.
	 * 
	 * @param request
	 *        The request
	 * @return The captured reference
	 * @see #setCapturedReference(Request, Reference)
	 */
	public static Reference getCapturedReference( Request request )
	{
		return (Reference) request.getAttributes().get( CAPTURED_REFERENCE );
	}

	/**
	 * The captured reference.
	 * 
	 * @param request
	 *        The request
	 * @param capturedReference
	 *        The captive reference
	 * @see #getCapturedReference(Request)
	 */
	public static void setCapturedReference( Request request, Reference capturedReference )
	{
		request.getAttributes().put( CAPTURED_REFERENCE, capturedReference );
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
	public CapturingRedirector( Context context, String targetTemplate, boolean hostRoot )
	{
		this( context, targetTemplate, hostRoot, MODE_SERVER_OUTBOUND );
	}

	/**
	 * Constructor.
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
	public CapturingRedirector( Context context, String targetPattern, boolean hostRoot, int mode )
	{
		super( context, targetPattern, mode );
		describe();
		this.hostRoot = hostRoot;
	}

	//
	// Redirector
	//

	@Override
	public void handle( Request request, Response response )
	{
		Reference resourceRef = request.getResourceRef();

		Reference rootRef = hostRoot ? null : request.getRootRef();
		if( rootRef == null )
			// The root reference could be null (e.g. in RIAP)
			rootRef = new Reference( resourceRef.getHostIdentifier() + "/" );

		setCapturedReference( request, new Reference( rootRef, resourceRef ) );

		super.handle( request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Whether to set the base reference to the host root URI.
	 */
	private final boolean hostRoot;

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "CapturingRedirector" );
		setDescription( "A redirector that keeps track of the captured reference" );
	}
}
