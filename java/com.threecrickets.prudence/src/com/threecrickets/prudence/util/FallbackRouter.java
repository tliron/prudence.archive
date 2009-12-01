package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

@SuppressWarnings("deprecation")
public class FallbackRouter extends Router
{
	public FallbackRouter( Context context )
	{
		super( context );
	}

	@Override
	public Route attach( String pathTemplate, Restlet target )
	{
		Route exists = null;
		for( Route route : getRoutes() )
		{
			if( route.getTemplate().getPattern().equals( pathTemplate ) )
			{
				exists = route;
				break;
			}
		}

		if( exists != null )
		{
			Restlet current = exists.getNext();
			if( current instanceof Fallback )
			{
				( (Fallback) current ).addRestlet( target );
			}
			else
			{
				// Replace current with Fallback
				Fallback fallback = new Fallback( getContext(), current, target );
				exists.setNext( fallback );
			}

			return exists;
		}

		return super.attach( pathTemplate, target );
	}
}
