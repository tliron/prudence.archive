package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

public class FallbackFilter extends Filter
{
	private final Restlet fallback;

	public FallbackFilter( Context context, Restlet next, Restlet fallback )
	{
		super( context, next );
		this.fallback = fallback;
	}

	@Override
	protected void afterHandle( Request request, Response response )
	{
		if( response.getStatus() == Status.CLIENT_ERROR_NOT_FOUND )
		{
			response.setStatus( Status.SUCCESS_OK );
			fallback.handle( request, response );
		}
	}
}
