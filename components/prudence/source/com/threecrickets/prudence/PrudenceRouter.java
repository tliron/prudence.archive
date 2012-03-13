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

import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import com.threecrickets.prudence.util.CapturingRouter;
import com.threecrickets.prudence.util.Fallback;
import com.threecrickets.prudence.util.FallbackRouter;
import com.threecrickets.prudence.util.NormalizingRedirector;
import com.threecrickets.prudence.util.ResolvingRouter;

/**
 * A {@link Router} with shortcut methods. Inherits all functionality from
 * {@link FallbackRouter}, {@link CapturingRouter} and {@link ResolvingRouter}.
 * 
 * @author Tal Liron
 */
public class PrudenceRouter extends FallbackRouter
{
	//
	// Construction
	//

	/**
	 * Constructs a Prudence router with a default {@link Fallback} cache
	 * duration of 5 seconds.
	 * 
	 * @param context
	 *        The context
	 */
	public PrudenceRouter( Context context )
	{
		super( context, 5000 );
		describe();
	}

	/**
	 * Constructs a Prudence router.
	 * 
	 * @param context
	 *        The context
	 * @param cacheDuration
	 *        The default cache duration for {@link Fallback} instances, in
	 *        milliseconds
	 */
	public PrudenceRouter( Context context, int cacheDuration )
	{
		super( context, cacheDuration );
		describe();
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
	@SuppressWarnings("unchecked")
	public TemplateRoute attach( String uriTemplate, String targetClassName ) throws ClassNotFoundException
	{
		return attach( uriTemplate, (Class<? extends ServerResource>) getClass().getClassLoader().loadClass( targetClassName ) );
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
	public TemplateRoute attachBase( String uriTemplate, String targetClassName ) throws ClassNotFoundException
	{
		TemplateRoute route = attach( uriTemplate, targetClassName );
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
	public TemplateRoute attachBase( String uriTemplate, Restlet target )
	{
		TemplateRoute route = attach( uriTemplate, target );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #attach(String, Restlet)}, but detaches the target first. The
	 * URI path template that must match the relative part of the resource URI
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public TemplateRoute reattach( String uriTemplate, Restlet target )
	{
		detach( target );
		return attach( uriTemplate, target );
	}

	/**
	 * As {@link #reattach(String, Restlet)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public TemplateRoute reattachBase( String uriTemplate, Restlet target )
	{
		TemplateRoute route = reattach( uriTemplate, target );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #filter(String, Filter, Restlet)}, but internally uses a
	 * {@link DelegatedFilter}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param documentName
	 *        The filter document name
	 * @param context
	 *        The context for the delegated filter
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public TemplateRoute filter( String uriTemplate, String documentName, Context context, Restlet target )
	{
		return filter( uriTemplate, new DelegatedFilter( context, documentName ), target );
	}

	/**
	 * As {@link #filter(String, Filter, Restlet)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param filter
	 *        The filter
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public TemplateRoute filterBase( String uriTemplate, Filter filter, Restlet target )
	{
		TemplateRoute route = filter( uriTemplate, filter, target );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #filter(String, String, Context, Restlet)}, but enforces
	 * matching mode {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param documentName
	 *        The filter document name
	 * @param context
	 *        The context for the delegated filter
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public TemplateRoute filterBase( String uriTemplate, String documentName, Context context, Restlet target )
	{
		return filterBase( uriTemplate, new DelegatedFilter( context, documentName ), target );
	}

	/**
	 * Redirects a URI to a new URI with HTTP status 307 ("temporary"). You can
	 * use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link NormalizingRedirector}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetUriTemplate
	 *        The target URI template
	 * @return The created route
	 * @see NormalizingRedirector
	 */
	public TemplateRoute redirectClient( String uriTemplate, String targetUriTemplate )
	{
		return redirectClient( uriTemplate, targetUriTemplate, 307 );
	}

	/**
	 * As {@link #redirectClient(String, String)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetUriTemplate
	 *        The target URI template
	 * @return The created route
	 * @see NormalizingRedirector
	 */
	public TemplateRoute redirectClientBase( String uriTemplate, String targetUriTemplate )
	{
		TemplateRoute route = redirectClient( uriTemplate, targetUriTemplate );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * Redirects a URI to a new URI. You can use template variables in the URIs.
	 * <p>
	 * Enforces matching mode {@link Template#MODE_EQUALS}.
	 * <p>
	 * This is handled via a {@link NormalizingRedirector}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetUriTemplate
	 *        The target URI template
	 * @param statusCode
	 *        HTTP status code (must be 301, 302, 303 or 307)
	 * @return The created route
	 * @see NormalizingRedirector
	 */
	public TemplateRoute redirectClient( String uriTemplate, String targetUriTemplate, int statusCode )
	{
		int mode;
		switch( statusCode )
		{
			case 301:
				mode = Redirector.MODE_CLIENT_PERMANENT;
				break;
			case 302:
				mode = Redirector.MODE_CLIENT_FOUND;
				break;
			case 303:
				mode = Redirector.MODE_CLIENT_SEE_OTHER;
				break;
			case 307:
				mode = Redirector.MODE_CLIENT_TEMPORARY;
				break;
			default:
				throw new IllegalArgumentException( "Unsupported status code: " + statusCode );
		}

		TemplateRoute route = attach( uriTemplate, new NormalizingRedirector( getContext(), targetUriTemplate, mode ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	/**
	 * As {@link #redirectClient(String, String, int)}, but enforces matching
	 * mode {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param targetUriTemplate
	 *        The target URI template
	 * @param statusCode
	 *        HTTP status code (must be 301, 302, 303 or 307)
	 * @return The created route
	 * @see NormalizingRedirector
	 */
	public TemplateRoute redirectClientBase( String uriTemplate, String targetUriTemplate, int statusCode )
	{
		TemplateRoute route = redirectClient( uriTemplate, targetUriTemplate, statusCode );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #capture(String, String, boolean)}, with capturing of query.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 */
	public TemplateRoute capture( String uriTemplate, String internalUriTemplate )
	{
		return capture( uriTemplate, internalUriTemplate, true );
	}

	/**
	 * As {@link #capture(String, String)}, but enforces matching mode
	 * {@link Template#MODE_STARTS_WITH}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @param captureQuery
	 *        Whether to capture the query, too
	 * @return The created route
	 */
	public TemplateRoute captureBase( String uriTemplate, String internalUriTemplate, boolean captureQuery )
	{
		TemplateRoute route = capture( uriTemplate, internalUriTemplate );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * As {@link #captureBase(String, String, boolean)}, with capturing of
	 * query.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 */
	public TemplateRoute captureBase( String uriTemplate, String internalUriTemplate )
	{
		return captureBase( uriTemplate, internalUriTemplate, true );
	}

	/**
	 * Shortcut for calling {@link #capture(String, String)} and
	 * {@link #hide(String)}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @param captureQuery
	 *        Whether to capture the query, too
	 * @return The created route (for the capture, not the hide)
	 */
	public TemplateRoute captureAndHide( String uriTemplate, String internalUriTemplate, boolean captureQuery )
	{
		TemplateRoute route = capture( uriTemplate, internalUriTemplate, captureQuery );
		hide( internalUriTemplate );
		return route;
	}

	/**
	 * As {@link #captureAndHide(String, String, boolean)}, with capturing of
	 * query.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 */
	public TemplateRoute captureAndHide( String uriTemplate, String internalUriTemplate )
	{
		return captureAndHide( uriTemplate, internalUriTemplate, true );
	}

	/**
	 * As {@link #captureOther(String, String, String, boolean)}, with capturing
	 * of query.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param application
	 *        The internal application name
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 */
	public TemplateRoute captureOther( String uriTemplate, String application, String internalUriTemplate )
	{
		return captureOther( uriTemplate, application, internalUriTemplate, true );
	}

	/**
	 * Shortcut for calling {@link #captureOther(String, String, String)} and
	 * {@link #hide(String)}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param application
	 *        The internal application name
	 * @param internalUriTemplate
	 *        The internal URI path to which we will redirect
	 * @return The created route
	 */
	public TemplateRoute captureOtherAndHide( String uriTemplate, String application, String internalUriTemplate )
	{
		TemplateRoute route = captureOther( uriTemplate, application, internalUriTemplate );
		hide( internalUriTemplate );
		return route;
	}

	/**
	 * Sets the URI to always return {@link Status#CLIENT_ERROR_NOT_FOUND}. Note
	 * that if there really is a resource at that URI, it might still be
	 * available via other routes.
	 * <p>
	 * Hiding is explicitly disabled for the RIAP protocol.
	 * 
	 * @param uri
	 *        The URI path that must match the remaining part of the resource
	 *        URI
	 */
	public void hide( String uri )
	{
		hiddenUris.add( uri );
	}

	//
	// Router
	//

	@Override
	public void handle( Request request, Response response )
	{
		// RIAP ignores hide
		if( request.getProtocol() != Protocol.RIAP )
		{
			String remaining = request.getResourceRef().getRemainingPart();
			if( hiddenUris.contains( remaining ) )
			{
				response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
				return;
			}
		}

		super.handle( request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Hidden URIs.
	 * 
	 * @see #hide(String)
	 */
	private CopyOnWriteArraySet<String> hiddenUris = new CopyOnWriteArraySet<String>();

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "PrudenceRouter" );
		setDescription( "A router with shortcut methods, inheriting functionality of FallbackRouter, CapturingRouter and ResolvingRouter" );
	}
}
