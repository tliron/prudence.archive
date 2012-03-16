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

package com.threecrickets.prudence;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;

/**
 * A Restlet {@link Application} that returns
 * {@link Status#CLIENT_ERROR_NOT_FOUND} when it is stopped.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 * @see Restlet#start()
 * @see Restlet#stop()
 */
public class PrudenceApplication extends Application
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public PrudenceApplication()
	{
		super();
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public PrudenceApplication( Context context )
	{
		super( context );
		describe();
	}

	//
	// Restlet
	//

	@Override
	public void handle( Request request, Response response )
	{
		if( isStarted() )
			super.handle( request, response );
		else
			response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( "PrudenceApplication" );
		setDescription( "A an application that does not route requests when it is stopped" );
	}
}
