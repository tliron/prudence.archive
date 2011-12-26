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
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.Variable;

/**
 * A {@link Router} that uses {@link ResolvingTemplate} for all routes.
 * 
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class ResolvingRouter extends Router
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public ResolvingRouter()
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
	public ResolvingRouter( Context context )
	{
		super( context );
		describe();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Router
	//

	@Override
	protected Route createRoute( String uriPattern, Restlet target, int matchingMode )
	{
		Route result = new Route( this, new ResolvingTemplate( uriPattern, matchingMode, Variable.TYPE_URI_SEGMENT, "", true, false ), target );
		result.setMatchingQuery( getDefaultMatchingQuery() );
		return result;
	}

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "ResolvingRouter" );
		setDescription( "A router that uses ResolvingTemplate for all routes" );
	}
}
