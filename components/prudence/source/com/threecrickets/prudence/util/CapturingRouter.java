package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;

/**
 * A {@link Router} that uses {@link CapturingRedirector} and
 * {@link CapturingRoute} to allow for URI capturing.
 * 
 * @author Tal Liron
 */
public class CapturingRouter extends ResolvingRouter
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public CapturingRouter( Context context )
	{
		super( context );
		describe();
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
	 * This is handled via a {@link CapturingRedirector} in
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
	 * @see CapturingRedirector
	 */
	public TemplateRoute capture( String uriTemplate, String internalUriTemplate, boolean captureQuery )
	{
		if( !internalUriTemplate.startsWith( "/" ) )
			internalUriTemplate = "/" + internalUriTemplate;
		String targetUriTemplate = "riap://application" + internalUriTemplate;
		if( captureQuery )
			targetUriTemplate += "?{rq}";
		TemplateRoute route = attach( uriTemplate, new CapturingRedirector( getContext(), targetUriTemplate, false ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	/**
	 * Internally redirects a URI to a new URI within any application installed
	 * in this router's component. You can use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link CapturingRedirector} in
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
	public TemplateRoute captureOther( String uriTemplate, String application, String internalUriTemplate, boolean captureQuery )
	{
		if( !internalUriTemplate.startsWith( "/" ) )
			internalUriTemplate = "/" + internalUriTemplate;
		String targetUriTemplate = "riap://component/" + application + internalUriTemplate;
		if( captureQuery )
			targetUriTemplate += "?{rq}";
		TemplateRoute route = attach( uriTemplate, new CapturingRedirector( getContext(), targetUriTemplate, false ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected TemplateRoute createRoute( String uriPattern, Restlet target, int matchingMode )
	{
		if( target instanceof CapturingRedirector )
		{
			// Use CapturingRoutes for CapturingRedirectors
			TemplateRoute result = new CapturingRoute( this, new ResolvingTemplate( uriPattern, matchingMode, Variable.TYPE_URI_SEGMENT, "", true, false ), target );
			result.setMatchingQuery( getDefaultMatchingQuery() );
			return result;
		}
		else
			return super.createRoute( uriPattern, target, matchingMode );
	}

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "CapturingRouter" );
		setDescription( "A router that uses CapturingRedirector and CapturingRoute to allow for URI capturing" );
	}
}
