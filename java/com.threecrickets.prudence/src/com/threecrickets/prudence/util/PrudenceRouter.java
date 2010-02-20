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
import org.restlet.Restlet;
import org.restlet.resource.Resource;
import org.restlet.routing.Redirector;
import org.restlet.routing.Route;
import org.restlet.routing.Template;
import org.restlet.util.Resolver;

/**
 * A {@link FallbackRouter} with shortcut methods for common routing tasks.
 * 
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class PrudenceRouter extends FallbackRouter
{
	//
	// Construction
	//

	/**
	 * Constructs a Prudence router with a default cache duration of 5 seconds.
	 * 
	 * @param context
	 *        The context
	 */
	public PrudenceRouter( Context context )
	{
		super( context );
	}

	/**
	 * Constructs a Prudence router.
	 * 
	 * @param context
	 *        The context
	 * @param cacheDuration
	 *        The cache duration, in milliseconds
	 */
	public PrudenceRouter( Context context, int cacheDuration )
	{
		super( context, cacheDuration );
		setName( "PrudenceRouter" );
		setDescription( "A FallbackRouter with shortcut methods for common routing tasks" );
	}

	//
	// Operations
	//

	/**
	 * Attach a {@link Resource} with the specified class name. The class is
	 * loaded using this class's class loader.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetClassName
	 *        The target Resource class to attach
	 * @return The created route
	 * @throws ClassNotFoundException
	 *         If the named class was not found
	 * @see #attach(String, Class)
	 */
	public Route attach( String pathTemplate, String targetClassName ) throws ClassNotFoundException
	{
		return attach( pathTemplate, getClass().getClassLoader().loadClass( targetClassName ) );
	}

	/**
	 * As {@link #attach(String, String)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetClassName
	 *        The target Resource class to attach
	 * @return The created route
	 * @throws ClassNotFoundException
	 *         If the named class was not found
	 * @see #attach(String, Class)
	 */
	public Route attachBase( String pathTemplate, String targetClassName ) throws ClassNotFoundException
	{
		Route route = attach( pathTemplate, targetClassName );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #attach(String, Restlet)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public Route attachBase( String pathTemplate, Restlet target )
	{
		Route route = attach( pathTemplate, target );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * Internally redirects a URI to a new URI, with support for URI templating.
	 * This is often called "URI rewriting."
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link Rewriter} in
	 * {@link Redirector#MODE_SERVER_DISPATCHER} mode.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param rewrittenPathTemplate
	 *        The URI path to which we will redirect
	 * @return The created route
	 * @see Rewriter
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route rewrite( String pathTemplate, String rewrittenPathTemplate )
	{
		Route route = attach( pathTemplate, new Rewriter( getContext(), rewrittenPathTemplate, Redirector.MODE_SERVER_DISPATCHER ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}
}
