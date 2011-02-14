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

package com.threecrickets.prudence;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

import com.threecrickets.prudence.service.RedirectorService;
import com.threecrickets.prudence.util.CaptiveRedirector;
import com.threecrickets.prudence.util.ResolvingRedirector;

/**
 * A {@link Filter} that wraps an underlying {@link DelegatedHandler}.
 * <p>
 * Supported entry points are:
 * <ul>
 * <li><code>handleBefore(conversation)</code></li>
 * <li><code>handleAfter(conversation)</code></li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class DelegatedFilter extends Filter
{
	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param documentName
	 *        The document name
	 */
	public DelegatedFilter( Context context, String documentName )
	{
		this( context, null, documentName );
	}

	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param documentName
	 *        The document name
	 */
	public DelegatedFilter( Context context, Restlet next, String documentName )
	{
		super( context, next );
		delegatedHandler = new DelegatedHandler( documentName, context );
	}

	//
	// Attributes
	//

	/**
	 * @return The entry point name for handleBefore
	 * @see #setEntryPointNameForBefore(String)
	 */
	public String getEntryPointNameForBefore()
	{
		return entryPointNameForBefore;
	}

	/**
	 * @param entryPointNameForBefore
	 *        The entry point name for handleBefore
	 * @see #getEntryPointNameForBefore()
	 */
	public void setEntryPointNameForBefore( String entryPointNameForBefore )
	{
		this.entryPointNameForBefore = entryPointNameForBefore;
	}

	/**
	 * @return The entry point name for handleAfter
	 * @see #setEntryPointNameForAfter(String)
	 */
	public String getEntryPointNameForAfter()
	{
		return entryPointNameForAfter;
	}

	/**
	 * @param entryPointNameForAfter
	 *        The entry point name for handleAfter
	 * @see #getEntryPointNameForAfter()
	 */
	public void setEntryPointNameForAfter( String entryPointNameForAfter )
	{
		this.entryPointNameForAfter = entryPointNameForAfter;
	}

	/**
	 * @return The default action to use for <code>handleBefore()</code> if none
	 *         is specified
	 * @see #setDefaultAction(int)
	 */
	public int getDefaultBeforeAction()
	{
		return defaultBeforeAction;
	}

	/**
	 * @param defaultBeforeAction
	 *        The default action to use for <code>handleBefore()</code> if none
	 *        is specified
	 * @see #getDefaultBeforeAction()
	 */
	public void setDefaultAction( int defaultBeforeAction )
	{
		this.defaultBeforeAction = defaultBeforeAction;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		RedirectorService redirectorService = new RedirectorService( Redirector.MODE_SERVER_OUTBOUND );
		Object r = delegatedHandler.handle( entryPointNameForBefore, redirectorService );

		if( r instanceof Number )
			// Handle number
			return ( (Number) r ).intValue();
		else if( r != null )
		{
			// Handle string
			String action = r.toString();
			if( action != null )
			{
				action = action.toUpperCase();
				if( action.equals( "CONTINUE" ) )
					return CONTINUE;
				else if( action.equals( "SKIP" ) )
					return SKIP;
				else if( action.equals( "STOP" ) )
					return STOP;
				else if( action.equals( "REDIRECT" ) )
				{
					Redirector redirector = new ResolvingRedirector( delegatedHandler.getAttributes().getContext(), redirectorService.getReference(), redirectorService.getMode() );
					redirector.handle( request, response );
					return STOP;
				}
				else if( action.equals( "CAPTURE" ) )
				{
					String reference = "riap://application/" + redirectorService.getReference() + "?{rq}";
					Redirector redirector = new CaptiveRedirector( delegatedHandler.getAttributes().getContext(), reference, false );
					redirector.handle( request, response );
					return STOP;
				}
			}
		}

		return defaultBeforeAction;
	}

	@Override
	protected void afterHandle( Request request, Response response )
	{
		delegatedHandler.handle( entryPointNameForAfter );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The delegated handler.
	 */
	private final DelegatedHandler delegatedHandler;

	/**
	 * The name of the <code>handleBefore()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForBefore = "handleBefore";

	/**
	 * The name of the <code>handleAfter()</code> entry point in the executable.
	 */
	private volatile String entryPointNameForAfter = "handleAfter";

	/**
	 * The default action to use for <code>handleBefore()</code> if none is
	 * specified.
	 */
	private volatile int defaultBeforeAction = CONTINUE;
}
