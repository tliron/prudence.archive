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

package com.threecrickets.prudence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Scripturian;

public class KitchenSink
{
	@BeforeClass
	public static void first()
	{
		Scripturian.main( new String[]
		{
			"instance"
		} );

		try
		{
			// Argh. :(
			Thread.sleep( 20000 );
		}
		catch( InterruptedException x )
		{
		}
	}

	@Test
	public void prudenceAdmin()
	{
		testOK( "/" );

		// /web/static/
		testOK( "/style/soft-cricket.css" );
	}

	@Test
	public void prudenceTest()
	{
		testOK( "/prudence-test/" );
		testRedirect( "/prudence-test" );

		// /web/static/
		testOK( "/prudence-test/style/soft-cricket.css" );

		// /web/dynamic/
		testOK( "/prudence-test/test/rhino?id=hello" );
		testOK( "/prudence-test/test/quercus?id=hello" );
		testOK( "/prudence-test/test/jython?id=hello" );
		testOK( "/prudence-test/test/jepp?id=hello" );
		testOK( "/prudence-test/test/jruby?id=hello" );
		testOK( "/prudence-test/test/groovy?id=hello" );
		testOK( "/prudence-test/test/clojure?id=hello" );
		testOK( "/prudence-test/test/velocity?id=hello" );

		// /resources/
		testOK( "/prudence-test/data/jython/" );
		testOK( "/prudence-test/data/jruby/" );
		testOK( "/prudence-test/data/groovy/" );
		testOK( "/prudence-test/data/clojure/" );
		testOK( "/prudence-test/data/rhino/" );
	}

	@Test
	public void stickstick()
	{
		testOK( "/stickstick/" );
		testRedirect( "/stickstick" );

		// /web/static/
		testOK( "/stickstick/style/soft-cricket.css" );

		// /resources/
		testOK( "/stickstick/notes/" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private void testOK( String uri )
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
	}

	private void testRedirect( String uri )
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
	}
}
