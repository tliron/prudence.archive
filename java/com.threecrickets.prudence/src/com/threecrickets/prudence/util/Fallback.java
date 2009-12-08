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
		this( context, new AtomicInteger( 5000 ), restlets );
	}

	public Fallback( Context context, AtomicInteger remember, Restlet... restlets )
	{
		super( context );
		this.remember = remember;
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
				remembered.remove( ref );
			}
			else
			{
				// Use remembered restlet
				node.restlet.handle( request, response );
				if( wasHandled( request, response ) )
					return;
			}
		}

		// Try all restlets in order
		for( Restlet restlet : restlets )
		{
			response.setStatus( Status.SUCCESS_OK );
			restlet.handle( request, response );
			if( wasHandled( request, response ) )
			{
				// Found a good one
				if( remember.get() > 0 )
				{
					// Remember this restlet
					// (erasing any previously remembered one)
					remembered.put( ref, new Node( restlet ) );
				}
				// Stop here
				return;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected boolean wasHandled( Request request, Response response )
	{
		Status status = response.getStatus();
		return !status.equals( Status.CLIENT_ERROR_NOT_FOUND ) && !status.equals( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final CopyOnWriteArrayList<Restlet> restlets = new CopyOnWriteArrayList<Restlet>();

	private final AtomicInteger remember;

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
