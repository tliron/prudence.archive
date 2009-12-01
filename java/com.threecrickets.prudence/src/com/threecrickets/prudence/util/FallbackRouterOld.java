package com.threecrickets.prudence.util;

import java.util.ArrayList;
import java.util.Collection;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * Works like BEST_MATCH.
 * 
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class FallbackRouterOld extends Router
{
	public static final String ROUTES_TRIED = FallbackRouterOld.class.getCanonicalName() + ".restletsTried";

	public FallbackRouterOld( Context context )
	{
		super( context );
		setRoutingMode( Router.MODE_CUSTOM );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Route getCustom( Request request, Response response )
	{
		// Find the route with the best score while ignoring routes we've
		// already tried
		Collection<Route> routesTried = (Collection<Route>) request.getAttributes().get( ROUTES_TRIED );
		Route result = null;
		float requiredScore = getRequiredScore();
		float bestScore = 0F;
		float score;
		for( final Route current : getRoutes() )
		{
			score = current.score( request, response );

			if( ( score > bestScore ) && ( score >= requiredScore ) && ( !routesTried.contains( current ) ) )
			{
				bestScore = score;
				result = current;
			}
		}

		if( result != null )
			routesTried.add( result );

		/*
		 * System.out.println( ":" ); System.out.println( result.getNext() );
		 * for( Route r : routesTried ) System.out.println( r.getNext() );
		 */

		return result;
	}

	@Override
	public void handle( Request request, Response response )
	{
		Collection<Route> routesTried = new ArrayList<Route>();
		request.getAttributes().put( ROUTES_TRIED, routesTried );

		// Keep trying until we don't get a CLIENT_ERROR_NOT_FOUND, or run out
		// of matching routes
		while( true )
		{
			Restlet next = getNext( request, response );
			if( next == null )
			{
				response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
				break;
			}

			doHandle( next, request, response );

			if( response.getStatus() != Status.CLIENT_ERROR_NOT_FOUND )
				break;
		}
	}
}
