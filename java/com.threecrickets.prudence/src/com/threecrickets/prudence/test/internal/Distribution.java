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

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.restlet.Component;

import com.threecrickets.scripturian.GlobalScope;
import com.threecrickets.scripturian.Scripturian;

/**
 * @author Tal Liron
 */
public abstract class Distribution extends MultiTest
{
	//
	// Construction
	//

	public Distribution( String name )
	{
		super( 40, 1 );
		this.name = name;
	}

	@Before
	public void startPrudence()
	{
		Scripturian.main( new String[]
		{
			"instance", "--base-path=build/" + name + "/content/"
		} );

		try
		{
			// Defrost/preheat
			Thread.sleep( 2000 );
		}
		catch( InterruptedException x )
		{
			// Restore interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@After
	public void stopPrudence()
	{
		Component component = (Component) GlobalScope.getInstance().getAttributes().get( "com.threecrickets.prudence.component" );
		assertNotNull( component );
		try
		{
			component.stop();
			System.out.println( "Stopped component." );
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
	}

	//
	// MultiTest
	//

	@Override
	public void test( int index )
	{
		for( Runnable test : adminTests )
			test.run();
		for( Runnable test : getPrudenceTests() )
			test.run();
		for( Runnable test : stickstickTests )
			test.run();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected abstract Runnable[] getPrudenceTests();

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String name;

	private static Runnable[] adminTests = new Runnable[]
	{
		new TestOK( "/" ),
		// /web/static/
		new TestOK( "/style/soft-cricket.css" )
	};

	private static Runnable[] stickstickTests = new Runnable[]
	{
		new TestOK( "/stickstick/" ), new TestRedirected( "/stickstick" ),
		// /web/static/
		new TestOK( "/stickstick/style/soft-cricket.css" )
		// /resources/
		//new TestOK( "/stickstick/data/" )
	};
}
