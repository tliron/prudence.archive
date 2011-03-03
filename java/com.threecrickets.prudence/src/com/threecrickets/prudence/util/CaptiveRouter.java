package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Redirector;
import org.restlet.routing.Route;
import org.restlet.routing.Template;
import org.restlet.routing.Variable;

/**
 * A {@link Router} that uses {@link CaptiveRedirector} and {@link CaptiveRoute}
 * to allow for URI capturing.
 * 
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class CaptiveRouter extends ResolvingRouter
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 */
	public CaptiveRouter( Context context )
	{
		super( context );
	}

	//
	// Operations
	//

	/**
	 * Captures (internally redirects) a URI to a new URI within this router's
	 * application. You can use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link CaptiveRedirector} in
	 * {@link Redirector#MODE_SERVER_OUTBOUND} mode.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @param captureQuery
	 *        Whether to capture the query, too
	 * @return The created route
	 * @see CaptiveRedirector
	 */
	public Route capture( String uriTemplate, String internalUriTemplate, boolean captureQuery )
	{
		if( !internalUriTemplate.startsWith( "/" ) )
			internalUriTemplate = "/" + internalUriTemplate;
		String targetUriTemplate = "riap://application" + internalUriTemplate;
		if( captureQuery )
			targetUriTemplate += "?{rq}";
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
	 * This is handled via a {@link CaptiveRedirector} in
	 * {@link Redirector#MODE_SERVER_OUTBOUND} mode.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param application
	 *        The internal application name
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @param captureQuery
	 *        Whether to capture the query, too
	 * @return The created route
	 */
	public Route captureOther( String uriTemplate, String application, String internalUriTemplate, boolean captureQuery )
	{
		if( !internalUriTemplate.startsWith( "/" ) )
			internalUriTemplate = "/" + internalUriTemplate;
		String targetUriTemplate = "riap://component/" + application + internalUriTemplate;
		if( captureQuery )
			targetUriTemplate += "?{rq}";
		Route route = attach( uriTemplate, new CaptiveRedirector( getContext(), targetUriTemplate, false ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected Route createRoute( String uriPattern, Restlet target, int matchingMode )
	{
		// Use CaptiveRoutes for CaptiveRedirectors
		Route result;
		if( target instanceof CaptiveRedirector )
			result = new CaptiveRoute( this, new ResolvingTemplate( uriPattern, matchingMode, Variable.TYPE_URI_SEGMENT, "", true, false ), target );
		else
			result = new Route( this, new ResolvingTemplate( uriPattern, matchingMode, Variable.TYPE_URI_SEGMENT, "", true, false ), target );
		result.setMatchingQuery( getDefaultMatchingQuery() );
		return result;
	}
}
