package com.threecrickets.prudence.util;

import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;

public class Fallback extends Restlet
{
	private final CopyOnWriteArrayList<Restlet> restlets = new CopyOnWriteArrayList<Restlet>();

	public Fallback( Context context, Restlet... restlets )
	{
		super( context );
		for( Restlet restlet : restlets )
			this.restlets.add( restlet );
	}

	public void addRestlet( Restlet restlet )
	{
		this.restlets.add( restlet );
	}

	@Override
	public void handle( Request request, Response response )
	{
		super.handle( request, response );
		for( Restlet restlet : restlets )
		{
			response.setStatus( Status.SUCCESS_OK );
			restlet.handle( request, response );
			if( response.getStatus() != Status.CLIENT_ERROR_NOT_FOUND )
				break;
		}
	}
}
