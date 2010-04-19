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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Redirector;
import org.restlet.service.StatusService;

/**
 * Allows delegating the handling of errors to specified restlets.
 * 
 * @author Tal Liron
 */
public class DelegatedStatusService extends StatusService
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public DelegatedStatusService( String sourceCodeUri )
	{
		super();
		setOverwriting( true );
		this.sourceCodeUri = sourceCodeUri;
	}

	public DelegatedStatusService()
	{
		this( null );
	}

	//
	// Attributes
	//

	/**
	 * A map of error statuses to target restlets. If no handler is mapped for a
	 * status, the default handling will kick in. (Modifiable by concurrent
	 * threads.)
	 * 
	 * @return The error handlers
	 */
	public ConcurrentMap<Integer, Restlet> getErrorHandlers()
	{
		return errorHandlers;
	}

	/**
	 * Whether we should show debugging information on errors.
	 * 
	 * @return True if debugging
	 * @see #setDebugging(boolean)
	 */
	public boolean isDebugging()
	{
		return isDebugging;
	}

	/**
	 * Whether we should show debugging information on errors.
	 * 
	 * @param isDebugging
	 *        True if debugging
	 * @see #isDebugging()
	 */
	public void setDebugging( boolean isDebugging )
	{
		this.isDebugging = isDebugging;
	}

	//
	// Operations
	//

	/**
	 * Sets the handler for an error status.
	 * 
	 * @param statusCode
	 *        The status code
	 * @param errorHandler
	 *        The error handler
	 */
	public void setHandler( int statusCode, Restlet errorHandler )
	{
		errorHandlers.put( statusCode, errorHandler );
	}

	/**
	 * Captures (internally redirects) an error status to a URI within an
	 * application. You can use template variables in the URI.
	 * <p>
	 * F This is handled via a {@link Redirector} with mode
	 * {@link Redirector#MODE_SERVER_OUTBOUND}.
	 * 
	 * @param statusCode
	 *        The status code
	 * @param application
	 *        The internal application name
	 * @param internalUriTemplate
	 *        The internal URI template to which we will redirect
	 * @param context
	 *        The context
	 */
	public void capture( int statusCode, String application, String internalUriTemplate, Context context )
	{
		String targetUriTemplate = "riap://component/" + application + "/" + internalUriTemplate;
		setHandler( statusCode, new CaptiveRedirector( context, targetUriTemplate, Redirector.MODE_SERVER_OUTBOUND ) );
	}

	/**
	 * Removes the handler for an error status.
	 * 
	 * @param status
	 *        The status code
	 */
	public void removeHandler( int status )
	{
		errorHandlers.remove( status );
	}

	//
	// StatusService
	//

	@Override
	public Representation getRepresentation( Status status, Request request, Response response )
	{
		if( isEnabled() )
		{
			if( request.getAttributes().containsKey( "com.threecrickets.prudence.util.DelegatedStatusService.passThrough" ) )
				// Pass through
				return response.getEntity();

			Restlet errorHandler = errorHandlers.get( status.getCode() );

			if( errorHandler != null )
			{
				// Clear the status
				response.setStatus( Status.SUCCESS_OK );

				// Delegate
				errorHandler.handle( request, response );

				// Return the status
				response.setStatus( status );

				Representation representation = response.getEntity();

				// Avoid caching, which could require other interchanges
				// with client that we can't handle from here
				representation.setExpirationDate( null );
				representation.setModificationDate( null );
				representation.setTag( null );

				request.getAttributes().put( "com.threecrickets.prudence.util.DelegatedStatusService.passThrough", true );
				return representation;
			}

			if( isDebugging() && ( status.getThrowable() != null ) )
			{
				request.getAttributes().put( "com.threecrickets.prudence.util.DelegatedStatusService.passThrough", true );
				return new DebugRepresentation( status, request, response, sourceCodeUri );
			}
		}

		return super.getRepresentation( status, request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Our map of status codes to error handlers.
	 */
	private final ConcurrentMap<Integer, Restlet> errorHandlers = new ConcurrentHashMap<Integer, Restlet>();

	private final String sourceCodeUri;

	/**
	 * Whether we are debugging.
	 */
	private volatile boolean isDebugging;
}
