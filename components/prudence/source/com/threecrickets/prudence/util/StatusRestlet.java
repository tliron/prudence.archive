/**
 * Copyright 2009-2012 Three Crickets LLC.
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
import org.restlet.data.Status;

/**
 * A restlet that sets a specific status.
 * 
 * @author Tal Liron
 */
public class StatusRestlet extends Restlet
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param status
	 *        The status to set
	 */
	public StatusRestlet( Status status )
	{
		describe();
		this.status = status;
	}

	//
	// Restlet
	//

	@Override
	public void handle( Request request, Response response )
	{
		response.setStatus( status );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The status to set.
	 */
	private final Status status;

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "StatusRestlet" );
		setDescription( "A restlet that sets a specific status" );
	}
}
