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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.routing.Filter;

/**
 * A {@link Filter} that renames the {@link Request}'s {@link Reference}.
 * <p>
 * The original reference's query is preserved.
 * 
 * @author Tal Liron
 * @see Reference
 */
public class Renamer extends Filter
{
	//
	// Construction
	//

	/**
	 * Construct a renamer.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param newReference
	 *        The new reference that will replace the request's reference
	 */
	public Renamer( Context context, Restlet next, Reference newReference )
	{
		super( context, next );
		this.newReference = newReference;
	}

	/**
	 * Construct a renamer.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param newReferenceUri
	 *        The new reference that will replace the request's reference
	 */
	public Renamer( Context context, Restlet next, String newReferenceUri )
	{
		super( context, next );
		this.newReference = new Reference( newReferenceUri );
	}

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		Reference reference = new Reference( newReference );
		reference.setQuery( request.getResourceRef().getQuery() );
		request.setResourceRef( reference );

		return CONTINUE;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The new reference that will replace the request's reference.
	 */
	private final Reference newReference;
}
