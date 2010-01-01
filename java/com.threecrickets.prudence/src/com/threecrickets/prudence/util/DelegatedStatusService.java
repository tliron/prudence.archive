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

package com.threecrickets.prudence.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
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
	public DelegatedStatusService()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param enabled
	 *        True to enable the service
	 */
	public DelegatedStatusService( boolean enabled )
	{
		super( enabled );
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

	//
	// Operations
	//

	/**
	 * Sets the handler for an error status.
	 * 
	 * @param status
	 *        The status code
	 * @param errorHandler
	 *        The error handler
	 */
	public void setHandler( int status, Restlet errorHandler )
	{
		errorHandlers.put( status, errorHandler );
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
		Restlet errorHandler = errorHandlers.get( status.getCode() );
		if( errorHandler != null )
		{
			// Clear the status
			response.setStatus( Status.SUCCESS_OK );

			// Delegate
			errorHandler.handle( request, response );

			// Return the status
			response.setStatus( status );

			return response.getEntity();
		}
		else
			return super.getRepresentation( status, request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ConcurrentMap<Integer, Restlet> errorHandlers = new ConcurrentHashMap<Integer, Restlet>();
}
