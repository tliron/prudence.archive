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

package com.threecrickets.prudence.test.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * @author Tal Liron
 */
public class TestRedirected implements Runnable
{
	//
	// Construction
	//

	public TestRedirected( String uri )
	{
		this.uri = uri;
	}

	//
	// Runnable
	//

	public void run()
	{
		ClientResource resource = new ClientResource( "http://localhost:8080" + uri );
		resource.setFollowingRedirects( false );
		try
		{
			resource.get().exhaust();
			assertEquals( "Testing redirection: \"" + uri + "\"", Status.REDIRECTION_PERMANENT, resource.getStatus() );
		}
		catch( ResourceException x )
		{
			fail( "Testing redirection: \"" + uri + "\": " + x.getMessage() );
		}
		catch( IOException x )
		{
			fail( "Testing: \"" + uri + "\": " + x.getMessage() );
		}
		finally
		{
			resource.release();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String uri;
}
