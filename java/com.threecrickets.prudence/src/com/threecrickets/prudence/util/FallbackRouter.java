/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class FallbackRouter extends Router
{
	//
	// Construction
	//

	public FallbackRouter( Context context )
	{
		super( context );
	}

	//
	// Attributes
	//

	public int getRemember()
	{
		return remember.get();
	}

	public void setRemember( int remember )
	{
		this.remember.set( remember );
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
				Fallback fallback = new Fallback( getContext(), remember, current, target );
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

	private final AtomicInteger remember = new AtomicInteger( 5000 );
}
