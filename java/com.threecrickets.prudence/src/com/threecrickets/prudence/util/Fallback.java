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

	public Fallback( Context context, Restlet... targets )
	{
		this( context, new AtomicInteger( 5000 ), targets );
	}

	public Fallback( Context context, AtomicInteger remember, Restlet... targets )
	{
		super( context );
		this.remember = remember;
		for( Restlet target : targets )
			addTarget( target );
	}

	//
	// Attributes
	//

	public List<Restlet> getTargets()
	{
		return targets;
	}

	public void addTarget( Restlet target )
	{
		this.targets.add( target );
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

		String reference = request.getResourceRef().getRemainingPart();
		Node node = remembered.get( reference );
		if( node != null )
		{
			if( System.currentTimeMillis() - node.timestamp > remember.get() )
			{
				// Invalidate
				remembered.remove( reference );
			}
			else
			{
				// Use remembered restlet
				node.target.handle( request, response );
				if( wasHandled( request, response ) )
					return;
			}
		}

		// Try all targets in order
		for( Restlet target : targets )
		{
			response.setStatus( Status.SUCCESS_OK );
			target.handle( request, response );
			if( wasHandled( request, response ) )
			{
				// Found a good one
				if( remember.get() > 0 )
				{
					// Remember this target
					// (erasing any previously remembered one)
					remembered.put( reference, new Node( target ) );
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

	private final CopyOnWriteArrayList<Restlet> targets = new CopyOnWriteArrayList<Restlet>();

	private final AtomicInteger remember;

	private final ConcurrentHashMap<String, Node> remembered = new ConcurrentHashMap<String, Node>();

	private static class Node
	{
		private Node( Restlet target )
		{
			this.target = target;
		}

		private final Restlet target;

		private final long timestamp = System.currentTimeMillis();
	}
}
