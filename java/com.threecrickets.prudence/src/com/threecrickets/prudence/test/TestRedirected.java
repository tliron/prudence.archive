package com.threecrickets.prudence.test;

import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestRedirected implements Runnable
{
	public TestRedirected( String uri )
	{
		this.uri = uri;
	}

	public void run()
	{
		ClientResource resource = new ClientResource( "http://localhost:8080" + uri );
		resource.setFollowingRedirects( false );
		try
		{
			resource.get();
			assertEquals( "Testing redirection: \"" + uri + "\"", Status.REDIRECTION_PERMANENT, resource.getStatus() );
		}
		catch( ResourceException x )
		{
			fail( "Testing redirection: \"" + uri + "\": " + x.getMessage() );
		}
		finally
		{
			resource.release();
		}
	}

	private final String uri;
}
