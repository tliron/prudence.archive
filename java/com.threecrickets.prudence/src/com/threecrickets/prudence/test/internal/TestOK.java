package com.threecrickets.prudence.test.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class TestOK implements Runnable
{
	public TestOK( String uri )
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
			assertEquals( "Testing: \"" + uri + "\"", Status.SUCCESS_OK, resource.getStatus() );
		}
		catch( ResourceException x )
		{
			fail( "Testing: \"" + uri + "\": " + x.getMessage() );
		}
		finally
		{
			resource.release();
		}
	}

	private final String uri;
}
