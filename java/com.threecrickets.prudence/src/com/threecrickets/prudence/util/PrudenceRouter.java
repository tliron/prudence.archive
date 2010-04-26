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
		super( context, 5000 );
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
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
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
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetClassName
	 *        The target Resource class to attach
	 * @return The created route
	 * @throws ClassNotFoundException
	 *         If the named class was not found
	 * @see #attach(String, Class)
	 */
	public Route attach( String uriTemplate, String targetClassName ) throws ClassNotFoundException
	{
		return attach( uriTemplate, getClass().getClassLoader().loadClass( targetClassName ) );
	}

	/**
	 * As {@link #attach(String, String)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetClassName
	 *        The target Resource class to attach
	 * @return The created route
	 * @throws ClassNotFoundException
	 *         If the named class was not found
	 * @see #attach(String, Class)
	 */
	public Route attachBase( String uriTemplate, String targetClassName ) throws ClassNotFoundException
	{
		Route route = attach( uriTemplate, targetClassName );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #attach(String, Restlet)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public Route attachBase( String uriTemplate, Restlet target )
	{
		Route route = attach( uriTemplate, target );
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
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param relativeUriTemplate
	 *        The URI path to which we will redirect
	 * @return The created route
	 * @see NormalizingRedirector
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route redirectRelative( String uriTemplate, String relativeUriTemplate )
	{
		String targetPathTemplate = "{ri}" + relativeUriTemplate;
		Route route = attach( uriTemplate, new NormalizingRedirector( getContext(), targetPathTemplate, Redirector.MODE_SERVER_INBOUND ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	/**
	 * Captures (internally redirects) a URI to a new URI within this router's
	 * application. You can use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link Redirector} in
	 * {@link Redirector#MODE_SERVER_OUTBOUND} mode.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 * @see NormalizingRedirector
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route capture( String uriTemplate, String internalUriTemplate )
	{
		String targetUriTemplate = "riap://application/" + internalUriTemplate + "?{rq}";
		Route route = attach( uriTemplate, new CaptiveRedirector( getContext(), targetUriTemplate, false ) );
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
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param application
	 *        The internal application name
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 * @see NormalizingRedirector
	 * @see Resolver#createResolver(Request, Response)
	 */
	public Route captureOther( String uriTemplate, String application, String internalUriTemplate )
	{
		String targetUriTemplate = "riap://component/" + application + "/" + internalUriTemplate + "?{rq}";
		Route route = attach( uriTemplate, new CaptiveRedirector( getContext(), targetUriTemplate, false ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}
}
