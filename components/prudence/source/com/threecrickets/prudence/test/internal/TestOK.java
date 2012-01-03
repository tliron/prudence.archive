/**
 * Copyright 2009-2012 Three Crickets LLC.
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
public class TestOK implements Runnable
{
	//
	// Construction
	//

	public TestOK( String uri )
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
			assertEquals( "Testing: \"" + uri + "\"", Status.SUCCESS_OK, resource.getStatus() );
		}
		catch( ResourceException x )
		{
			fail( "Testing: \"" + uri + "\": " + x.getMessage() );
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
