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
import org.restlet.resource.ServerResource;
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
	 * Attach a {@link ServerResource} with the specified class name. The class
	 * is loaded using this class's class loader.
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
	 * Redirects a URI to a new URI relative to the original. You can use
	 * template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link NormalizingRedirector} in
	 * {@link Redirector#MODE_SERVER_INBOUND} mode.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param relativePathTemplate
	 *        The URI path to which we will redirect
	 * @return The created route
	 * @see NormalizingRedirector
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route redirectRelative( String pathTemplate, String relativePathTemplate )
	{
		String targetPathTemplate = "{ri}" + relativePathTemplate;
		Route route = attach( pathTemplate, new NormalizingRedirector( getContext(), targetPathTemplate, Redirector.MODE_SERVER_INBOUND ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	/**
	 * Internally redirects a URI to a new URI within this router's application.
	 * You can use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link Redirector} in
	 * {@link Redirector#MODE_SERVER_OUTBOUND} mode.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalPathTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 * @see NormalizingRedirector
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route capture( String pathTemplate, String internalPathTemplate )
	{
		String targetPathTemplate = "riap://application/" + internalPathTemplate;
		Route route = attach( pathTemplate, new Redirector( getContext(), targetPathTemplate, Redirector.MODE_SERVER_OUTBOUND ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	/**
	 * Internally redirects a URI to a new URI within any application installed
	 * in this router's component. You can use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link Redirector} in
	 * {@link Redirector#MODE_SERVER_OUTBOUND} mode.
	 * 
	 * @param pathTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param application
	 *        The internal application name
	 * @param internalPathTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 * @see NormalizingRedirector
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route captureOther( String pathTemplate, String application, String internalPathTemplate )
	{
		String targetPathTemplate = "riap://component/" + application + "/" + internalPathTemplate;
		Route route = attach( pathTemplate, new Redirector( getContext(), targetPathTemplate, Redirector.MODE_SERVER_OUTBOUND ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}
}
