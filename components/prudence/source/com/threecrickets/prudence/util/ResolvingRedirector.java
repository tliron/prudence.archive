/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

/**
 * A {@link Redirector} that uses {@link ResolvingTemplate}.
 * 
 * @author Tal Liron
 */
public class ResolvingRedirector extends Redirector
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param context
	 * @param targetPattern
	 * @param mode
	 */
	public ResolvingRedirector( Context context, String targetPattern, int mode )
	{
		super( context, targetPattern, mode );
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 * @param targetTemplate
	 */
	public ResolvingRedirector( Context context, String targetTemplate )
	{
		super( context, targetTemplate );
		describe();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Redirector
	//

	@Override
	protected Reference getTargetRef( Request request, Response response )
	{
		// This is essentially the original Restlet code modified to use
		// ResolvingTemplate.

		// Create the template
		Template rt = new ResolvingTemplate( this.targetTemplate );
		rt.setLogger( getLogger() );

		// Return the formatted target URI
		return new Reference( request.getResourceRef(), rt.format( request, response ) );
	}

	@Override
	protected void serverRedirect( Restlet next, Reference targetRef, Request request, Response response )
	{
		// This is essentially the original Restlet code modified to use
		// ResolvingTemplate.

		if( next == null )
			getLogger().warning( "No next Restlet provided for server redirection to " + targetRef );
		else
		{
			// Save the base URI if it exists as we might need it for
			// redirections
			Reference resourceRef = request.getResourceRef();
			Reference baseRef = resourceRef.getBaseRef();

			// Reset the protocol and let the dispatcher handle the protocol
			request.setProtocol( null );

			// Update the request to cleanly go to the target URI
			request.setResourceRef( targetRef );
			request.getAttributes().remove( HeaderConstants.ATTRIBUTE_HEADERS );
			next.handle( request, response );

			// Allow for response rewriting and clean the headers
			response.setEntity( rewrite( response.getEntity() ) );
			response.getAttributes().remove( HeaderConstants.ATTRIBUTE_HEADERS );
			request.setResourceRef( resourceRef );

			// In case of redirection, we may have to rewrite the redirect URI
			if( response.getLocationRef() != null )
			{
				Template rt = new ResolvingTemplate( this.targetTemplate );
				rt.setLogger( getLogger() );
				int matched = rt.parse( response.getLocationRef().toString(), request );

				if( matched > 0 )
				{
					String remainingPart = (String) request.getAttributes().get( "rr" );

					if( remainingPart != null )
						response.setLocationRef( baseRef.toString() + remainingPart );
				}
			}
		}
	}

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( "ResolvingRedirector" );
		setDescription( "A redirector that uses ResolvingTemplate" );
	}
}
