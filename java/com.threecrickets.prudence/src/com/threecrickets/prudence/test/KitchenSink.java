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

import org.junit.BeforeClass;

import com.threecrickets.scripturian.Scripturian;

/**
 * Results: Rhino 8.2 Quercus 43 Jython 6.4 Jepp 54.1 JRuby 6.3 Groovy 7.4
 * Clojure 9.9 Velocity 7.2
 * 
 * @author Tal Liron
 */
public class KitchenSink extends MultiTest
{
	public KitchenSink()
	{
		super( 40, 1 );
	}

	@BeforeClass
	public static void first()
	{
		Scripturian.main( new String[]
		{
			"instance"
		} );

		try
		{
			// Defrost/preheat
			Thread.sleep( 5000 );
		}
		catch( InterruptedException x )
		{
			// Restore interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void test( int index )
	{
		for( Runnable test : adminTests )
			test.run();
		for( Runnable test : prudenceTests )
			test.run();
		for( Runnable test : stickstickTests )
			test.run();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Runnable[] adminTests = new Runnable[]
	{
		new TestOK( "/" ),
		// /web/static/
		new TestOK( "/style/soft-cricket.css" )
	};

	private static Runnable[] prudenceTests = new Runnable[]
	{
		new TestOK( "/prudence-test/" ), new TestRedirected( "/prudence-test" ),
		// /web/static/
		new TestOK( "/prudence-test/style/soft-cricket.css" ),
		// /web/dynamic/
		new TestOK( "/prudence-test/test/rhino/?id=hello" ),
		// new TestOK( "/prudence-test/test/quercus/?id=hello" ),
		new TestOK( "/prudence-test/test/jython/?id=hello" ),
		// new TestOK( "/prudence-test/test/jepp/?id=hello" ),
		// new TestOK( "/prudence-test/test/jruby/?id=hello" ),
		new TestOK( "/prudence-test/test/groovy/?id=hello" ), new TestOK( "/prudence-test/test/clojure/?id=hello" ), new TestOK( "/prudence-test/test/velocity/?id=hello" ),
		// /resources/
		new TestOK( "/prudence-test/data/jython/" ),
		// new TestOK( "/prudence-test/data/jruby/" ),
		new TestOK( "/prudence-test/data/groovy/" ), new TestOK( "/prudence-test/data/clojure/" ), new TestOK( "/prudence-test/data/rhino/" )
	};

	private static Runnable[] stickstickTests = new Runnable[]
	{
		new TestOK( "/stickstick/" ), new TestRedirected( "/stickstick" ),
		// /web/static/
		new TestOK( "/stickstick/style/soft-cricket.css" ),
		// /resources/
		new TestOK( "/stickstick/data/" )
	};
}
