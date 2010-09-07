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

package com.threecrickets.prudence;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Route;
import org.restlet.routing.Template;

import com.threecrickets.prudence.util.CaptiveRedirector;
import com.threecrickets.prudence.util.Fallback;
import com.threecrickets.prudence.util.FallbackRouter;
import com.threecrickets.prudence.util.NormalizingRedirector;
import com.threecrickets.prudence.util.StatusRestlet;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentSource;

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
	 * Constructs a Prudence router with a default {@link Fallback} cache
	 * duration of 5 seconds.
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
	 *        The default cache duration for {@link Fallback} instances, in
	 *        milliseconds
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
	// Attributes
	//

	/**
	 * @return The filter document source
	 * @see #setFilterDocumentSource(DocumentSource)
	 */
	public DocumentSource<Executable> getFilterDocumentSource()
	{
		return filterDocumentSource;
	}

	/**
	 * @param filterDocumentSource
	 *        The filter document source
	 * @see #getFilterDocumentSource()
	 */
	public void setFilterDocumentSource( DocumentSource<Executable> filterDocumentSource )
	{
		this.filterDocumentSource = filterDocumentSource;
	}

	/**
	 * @return The filter language manager
	 * @see #setFilterLanguageManager(LanguageManager)
	 */
	public LanguageManager getFilterLanguageManager()
	{
		return filterLanguageManager;
	}

	/**
	 * @param filterLanguageManager
	 *        The filter language manager
	 * @see #getFilterLanguageManager()
	 */
	public void setFilterLanguageManager( LanguageManager filterLanguageManager )
	{
		this.filterLanguageManager = filterLanguageManager;
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
	 * Detaches the target restlet, and attaches the filter instead with the
	 * target chained after it.
	 * <p>
	 * The filter's context will be set to that of the target if it doesn't
	 * already have a context.
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
	public Route filter( String uriTemplate, Filter filter, Restlet target )
	{
		detach( target );
		if( filter.getContext() == null )
			filter.setContext( target.getContext() );
		filter.setNext( target );
		return attach( uriTemplate, filter );
	}

	/**
	 * As {@link #filter(String, Filter, Restlet)}, but internally uses a
	 * {@link DelegatedFilter}.
	 * <p>
	 * You must set {@link #setFilterDocumentSource(DocumentSource)} and
	 * {@link #setFilterLanguageManager(LanguageManager)} for this to work.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param documentName
	 *        The filter document name
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public Route filter( String uriTemplate, String documentName, Restlet target )
	{
		return filter( uriTemplate, new DelegatedFilter( null, documentName, filterDocumentSource, filterLanguageManager ), target );
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
	public Route filterBase( String uriTemplate, Filter filter, Restlet target )
	{
		detach( target );
		if( filter.getContext() == null )
			filter.setContext( target.getContext() );
		filter.setNext( target );
		return attachBase( uriTemplate, filter );
	}

	/**
	 * As
	 * {@link #filter(String, String, Restlet, DocumentSource, LanguageManager)}
	 * , but enforces matching mode {@link Template#MODE_STARTS_WITH}.
	 * <p>
	 * You must set {@link #setFilterDocumentSource(DocumentSource)} and
	 * {@link #setFilterLanguageManager(LanguageManager)} for this to work.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @param documentName
	 *        The filter document name
	 * @param target
	 *        The target Restlet to attach
	 * @return The created route
	 */
	public Route filterBase( String uriTemplate, String documentName, Restlet target )
	{
		return filterBase( uriTemplate, new DelegatedFilter( null, documentName, filterDocumentSource, filterLanguageManager ), target );
	}

	/**
	 * Redirects a URI to a new URI with HTTP status 303 ("see other"). You can
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
	public Route redirectClient( String uriTemplate, String targetUriTemplate )
	{
		return redirectClient( uriTemplate, targetUriTemplate, 303 );
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
	public Route redirectClient( String uriTemplate, String targetUriTemplate, int statusCode )
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

		Route route = attach( uriTemplate, new NormalizingRedirector( getContext(), targetUriTemplate, mode ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

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
	 * @return The created route
	 * @see CaptiveRedirector
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
	 * @return The created route
	 * @see CaptiveRedirector
	 */
	public Route captureOther( String uriTemplate, String application, String internalUriTemplate )
	{
		String targetUriTemplate = "riap://component/" + application + "/" + internalUriTemplate + "?{rq}";
		Route route = attach( uriTemplate, new CaptiveRedirector( getContext(), targetUriTemplate, false ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	/**
	 * Sets the URI to always return {@link Status#CLIENT_ERROR_NOT_FOUND}. Note
	 * that if there really is a resource at that URI, it might still be
	 * available via other routes.
	 * <p>
	 * Internally uses a {@link StatusRestlet}.
	 * 
	 * @param uriTemplate
	 *        The URI path template that must match the relative part of the
	 *        resource URI
	 * @return The created route
	 */
	public Route hide( String uriTemplate )
	{
		Route route = attach( uriTemplate, new StatusRestlet( Status.CLIENT_ERROR_NOT_FOUND ) );
		route.setMatchingMode( Template.MODE_EQUALS );
		return route;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Document source for delegated filters.
	 */
	private volatile DocumentSource<Executable> filterDocumentSource;

	/**
	 * Language manager for delegated filters.
	 */
	private volatile LanguageManager filterLanguageManager;
}
