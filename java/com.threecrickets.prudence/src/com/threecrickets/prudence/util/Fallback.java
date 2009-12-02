package com.threecrickets.prudence.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;

/**
 * @author Tal Liron
 */
public class Fallback extends Restlet
{
	//
	// Construction
	//

	public Fallback( Context context, Restlet... restlets )
	{
		super( context );
		for( Restlet restlet : restlets )
			addRestlet( restlet );
	}

	//
	// Attributes
	//

	public List<Restlet> getRestlets()
	{
		return restlets;
	}

	public void addRestlet( Restlet restlet )
	{
		this.restlets.add( restlet );
	}

	public int getRemember()
	{
		return remember.get();
	}

	public void setRemember( int remember )
	{
		this.remember.set( remember );
		if( remember == 0 )
			// Remember might have changed already, but it doesn't matter enough
			// to complicate things
			remembered.clear();
	}

	//
	// Restlet
	//

	@Override
	public void handle( Request request, Response response )
	{
		super.handle( request, response );

		String ref = request.getResourceRef().getRemainingPart();
		Node node = remembered.get( ref );
		if( node != null )
		{
			if( System.currentTimeMillis() - node.timestamp > remember.get() )
			{
				// Invalidate
				remembered.remove( node );
			}
			else
			{
				// Use remembered restlet
				node.restlet.handle( request, response );
				return;
			}
		}

		// Try all restlets in order
		for( Restlet restlet : restlets )
		{
			response.setStatus( Status.SUCCESS_OK );
			restlet.handle( request, response );
			if( response.getStatus() != Status.CLIENT_ERROR_NOT_FOUND )
			{
				// Found a good one
				if( remember.get() > 0 )
					// Remember this restlet
					remembered.put( ref, new Node( restlet ) );
				return;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final CopyOnWriteArrayList<Restlet> restlets = new CopyOnWriteArrayList<Restlet>();

	private final AtomicInteger remember = new AtomicInteger( 5000 );

	private final ConcurrentHashMap<String, Node> remembered = new ConcurrentHashMap<String, Node>();

	private static class Node
	{
		private Node( Restlet restlet )
		{
			this.restlet = restlet;
		}

		private final Restlet restlet;

		private final long timestamp = System.currentTimeMillis();
	}
}
