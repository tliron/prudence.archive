package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.routing.Filter;

/**
 * @author Tal Liron
 */
public class Renamer extends Filter
{
	//
	// Construction
	//

	public Renamer( Context context, Restlet next, Reference newReference )
	{
		super( context, next );
		this.newReference = newReference;
	}

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

	private final Reference newReference;
}
