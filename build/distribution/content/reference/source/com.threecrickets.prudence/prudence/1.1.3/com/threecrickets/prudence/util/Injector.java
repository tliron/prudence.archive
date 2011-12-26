package com.threecrickets.prudence.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

/**
 * A {@link Filter} that adds values to the request attributes before moving to
 * the next restlet.
 * 
 * @author Tal Liron
 */
public class Injector extends Filter
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public Injector()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public Injector( Context context )
	{
		super( context );
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 */
	public Injector( Context context, Restlet next )
	{
		super( context, next );
	}

	//
	// Attributes
	//

	/**
	 * The values to be added to the request attributes.
	 * 
	 * @return The values
	 */
	public ConcurrentMap<String, Object> getValues()
	{
		return values;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		request.getAttributes().putAll( values );
		return CONTINUE;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The values to be added to the request attributes.
	 */
	private ConcurrentMap<String, Object> values = new ConcurrentHashMap<String, Object>();
}
