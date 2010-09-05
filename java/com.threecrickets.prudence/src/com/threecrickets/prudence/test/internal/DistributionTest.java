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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.restlet.Component;

import com.threecrickets.scripturian.GlobalScope;
import com.threecrickets.scripturian.Scripturian;

/**
 * @author Tal Liron
 */
public abstract class DistributionTest extends MultiTest
{
	//
	// Construction
	//

	public DistributionTest( String name, boolean enabled )
	{
		// threads=40, iterations=1
		super( enabled ? 20 : 0, enabled ? 10 : 0 );
		this.name = name;
	}

	@Before
	public void startPrudence()
	{
		if( started || ( ( threads == 0 ) && ( iterations == 0 ) ) )
			return;

		if( inProcess )
			startInProcessComponent();
		else
			startExternalProcess();

		assertTrue( started );

		// Disable Restlet client log messages
		Logger.getLogger( "" ).setLevel( Level.WARNING );
	}

	@AfterClass
	public static void stopPrudence()
	{
		if( !started )
			return;

		if( inProcess )
		{
			assertNotNull( getInProcessComponent() );
			stopInProcessComponent();
		}
		else
		{
			assertNotNull( externalProcess );
			stopExternalProcess();
		}

		started = false;
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

	private static final boolean inProcess = false;

	private static boolean started;

	private static Process externalProcess;

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
	// new TestOK( "/stickstick/data/" )
	};

	private final String name;

	private static Component getInProcessComponent()
	{
		return (Component) GlobalScope.getInstance().getAttributes().get( "com.threecrickets.prudence.component" );
	}

	private void startInProcessComponent()
	{
		System.out.println( "Starting Prudence instance in this process..." );

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
			x.printStackTrace();
		}

		assertNotNull( getInProcessComponent() );

		started = true;
	}

	private static void stopInProcessComponent()
	{
		try
		{
			getInProcessComponent().stop();
			System.out.println( "Stopped in-process component." );
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
	}

	private void startExternalProcess()
	{
		System.out.println( "Starting Prudence instance in an external process..." );

		try
		{
			externalProcess = Runtime.getRuntime().exec( "build/" + name + "/content/bin/run.sh" );
			try
			{
				BufferedReader input = new BufferedReader( new InputStreamReader( externalProcess.getInputStream() ) );
				String line;
				while( ( line = input.readLine() ) != null )
				{
					System.out.println( line );
					if( line.startsWith( "Finished all startup tasks" ) )
					{
						started = true;
						break;
					}
				}
				input.close();
			}
			catch( Exception x )
			{
				x.printStackTrace();
			}
		}
		catch( IOException x )
		{
			x.printStackTrace();
		}
	}

	private static void stopExternalProcess()
	{
		externalProcess.destroy();

		try
		{
			externalProcess.waitFor();
		}
		catch( InterruptedException x )
		{
			// Restore interrupt status
			Thread.currentThread().interrupt();
			x.printStackTrace();
		}

		System.out.println( "Stopped external process." );
		externalProcess = null;
	}

	static
	{
		// Make sure we stop the external process
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				if( externalProcess != null )
					stopExternalProcess();
			}
		} );
	}
}
