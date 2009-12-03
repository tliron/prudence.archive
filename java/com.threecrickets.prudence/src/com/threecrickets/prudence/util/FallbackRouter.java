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
				( (Fallback) current ).addRestlet( target );
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final AtomicInteger remember = new AtomicInteger( 5000 );
}
