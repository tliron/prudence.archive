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
 * A restlet that delegates {@link Restlet#handle(Request, Response)} to a
 * series of targets in sequence, stopping at the first target that satisfies
 * the condition of {@link #wasHandled(Request, Response)}. This is very useful
 * for allowing multiple restlets a chance to handle a request, while
 * "falling back" to subsequent restlets when those "fail."
 * <p>
 * If none of the targets "succeeds" in this respect, the response will be left
 * in the same condition as it was in the last attempt.
 * <p>
 * Supports a simple timed cache that "remembers" which target handled which
 * reference, in order to avoid unnecessary attempts on targets known to fail.
 * Note that in situations in which targets may sometimes fail and sometimes
 * succeed for the same reference, you would want to disable the cache or keep
 * it low.
 * <p>
 * By default, {@link #wasHandled(Request, Response)} checks that the response
 * status is not {@link Status#CLIENT_ERROR_NOT_FOUND} or
 * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
 * 
 * @author Tal Liron
 */
public class Fallback extends Restlet
{
	//
	// Construction
	//

	/**
	 * Construct a fallback for an array of target restlets with a default cache
	 * duration of 5 seconds.
	 * 
	 * @param context
	 *        The context
	 * @param targets
	 *        The target restlets
	 */
	public Fallback( Context context, Restlet... targets )
	{
		this( context, new AtomicInteger( 5000 ), targets );
	}

	/**
	 * Construct a fallback for an array of target restlets.
	 * 
	 * @param context
	 *        The context
	 * @param cacheDuration
	 *        The cache duration, in milliseconds
	 * @param targets
	 *        The target restlets
	 */
	public Fallback( Context context, AtomicInteger cacheDuration, Restlet... targets )
	{
		super( context );
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "Fallback" );
		setDescription( "Delegates to a series of targets in sequence, stopping at the first target that handles the request" );
		this.cacheDuration = cacheDuration;
		for( Restlet target : targets )
			addTarget( target );
	}

	//
	// Attributes
	//

	/**
	 * The targets restlets. (Modifiable by concurrent threads.)
	 * 
	 * @return The target restlets
	 */
	public List<Restlet> getTargets()
	{
		return targets;
	}

	/**
	 * Adds a target restlet.
	 * 
	 * @param target
	 *        A target restlet
	 */
	public void addTarget( Restlet target )
	{
		this.targets.add( target );
	}

	/**
	 * The cache duration, in milliseconds.
	 * 
	 * @return The cache duration, in milliseconds
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
	 */
	public void setCacheDuration( int cacheDuration )
	{
		this.cacheDuration.set( cacheDuration );
	}

	//
	// Restlet
	//

	@Override
	public void handle( Request request, Response response )
	{
		super.handle( request, response );

		String reference = request.getResourceRef().getRemainingPart();
		Node node = cache.get( reference );
		if( node != null )
		{
			if( System.currentTimeMillis() - node.timestamp > cacheDuration.get() )
			{
				// Invalidate
				cache.remove( reference );
			}
			else
			{
				// Use cached restlet
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
				if( cacheDuration.get() > 0 )
				{
					// Cache this target
					// (erasing any previously cached one)
					cache.put( reference, new Node( target ) );
				}
				// Stop here
				return;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Checks to see if a request was handled after a call to
	 * {@link Restlet#handle(Request, Response)}.
	 * <p>
	 * This default implementation checks that the response status is not
	 * {@link Status#CLIENT_ERROR_NOT_FOUND} or
	 * {@link Status#CLIENT_ERROR_METHOD_NOT_ALLOWED}.
	 * 
	 * @param request
	 *        The request
	 * @param response
	 *        The response
	 * @return "True" if the request was handled
	 */
	protected boolean wasHandled( Request request, Response response )
	{
		Status status = response.getStatus();
		// TODO: temp added 500 check for Directory bug
		return !status.equals( Status.CLIENT_ERROR_NOT_FOUND ) && !status.equals( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED ) && !status.equals( Status.SERVER_ERROR_INTERNAL );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The target restlets.
	 */
	private final CopyOnWriteArrayList<Restlet> targets = new CopyOnWriteArrayList<Restlet>();

	/**
	 * The cache duration, in milliseconds.
	 */
	private final AtomicInteger cacheDuration;

	/**
	 * The cache (references mapped to nodes).
	 */
	private final ConcurrentHashMap<String, Node> cache = new ConcurrentHashMap<String, Node>();

	/**
	 * A cached node.
	 */
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
