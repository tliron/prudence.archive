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

import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * A {@link Router} that takes care to bunch identical routes under
 * {@link Fallback} restlets. This is very useful for allowing multiple restlets
 * a chance to handle a request, while "falling back" to subsequent restlets
 * when those "fail."
 * <p>
 * Both {@link Router#attach(String, Restlet)} and
 * {@link Router#detach(Restlet)} are overriden to support bunching and
 * un-bunching. Note that if you otherwise change the routes (say, via modifying
 * {@link Router#getRoutes()}, you will override this behavior.
 * 
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class FallbackRouter extends Router
{
	//
	// Construction
	//

	/**
	 * Constructs a fallback router with a default cache duration of 5 seconds.
	 * 
	 * @param context
	 *        The context
	 */
	public FallbackRouter( Context context )
	{
		this( context, 5000 );
	}

	/**
	 * Construct a fallback router.
	 * 
	 * @param context
	 *        The context
	 * @param cacheDuration
	 *        The cache duration, in milliseconds
	 */
	public FallbackRouter( Context context, int cacheDuration )
	{
		super( context );
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "FallbackRouter" );
		setDescription( "A router that takes care to bunch identical routes under Fallback restlets" );
		this.cacheDuration = new AtomicInteger( cacheDuration );
	}

	//
	// Attributes
	//

	/**
	 * The cache duration, in milliseconds.
	 * 
	 * @return The cache duration, in milliseconds
	 * @see Fallback#getCacheDuration()
	 */
	public int getCacheDuration()
	{
		return cacheDuration.get();
	}

	/**
	 * The cache duration, in milliseconds. (Modifiable by concurrent threads.)
	 * 
	 * @param cacheDuration
	 *        The cache duration, in milliseconds
	 * @see Fallback#setCacheDuration(int)
	 */
	public void setCacheDuration( int cacheDuration )
	{
		this.cacheDuration.set( cacheDuration );
	}

	//
	// Router
	//

	@Override
	public Route attach( String pathTemplate, Restlet target )
	{
		Route existingRoute = null;
		for( Route route : getRoutes() )
		{
			if( route.getTemplate().getPattern().equals( pathTemplate ) )
			{
				existingRoute = route;
				break;
			}
		}

		if( existingRoute != null )
		{
			Restlet current = existingRoute.getNext();
			if( current instanceof Fallback )
			{
				// Add to current Fallback
				( (Fallback) current ).addTarget( target );
			}
			else
			{
				// Replace current target with Fallback
				Fallback fallback = new Fallback( getContext(), cacheDuration, current, target );
				existingRoute.setNext( fallback );
			}

			return existingRoute;
		}

		return super.attach( pathTemplate, target );
	}

	@Override
	public void detach( Restlet target )
	{
		boolean found = false;
		Route dead = null;
		for( Route route : getRoutes() )
		{
			Restlet restlet = route.getNext();
			if( restlet instanceof Fallback )
			{
				Fallback fallback = (Fallback) restlet;

				for( Restlet fallbackRestlet : fallback.getTargets() )
				{
					if( fallbackRestlet == target )
					{
						found = true;
						break;
					}
				}

				if( found )
				{
					fallback.getTargets().remove( target );
					int size = fallback.getTargets().size();
					if( size == 1 )
						// No need for fallback anymore
						route.setNext( fallback.getTargets().get( 0 ) );
					else if( size == 0 )
						// No need for route anymore
						dead = route;
					break;
				}
			}
		}

		if( dead != null )
			getRoutes().remove( dead );
		else if( !found )
			super.detach( target );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The cache duration, in milliseconds.
	 */
	private final AtomicInteger cacheDuration;
}
