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

import java.util.Random;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.junit.Test;

/**
 * @author Tal Liron
 */
public abstract class MultiTest
{
	//
	// Construction
	//

	public MultiTest( int threads, int iterations )
	{
		this.threads = threads;
		this.iterations = iterations;
	}

	//
	// Operations
	//

	public abstract void test( int index );

	//
	// JUnit
	//

	@Test
	public void singleThread() throws Throwable
	{
		for( int i = 0; i < iterations; i++ )
			test( 0 );
	}

	@Test
	public void multiThreaded() throws Throwable
	{
		if( threads == 0 )
			return;

		MultiTest.StressTest[] tests = new MultiTest.StressTest[threads];
		for( int i = 0; i < tests.length; i++ )
		{
			if( i % 2 == 0 )
			{
				// Even-numbered tests will have identical timing
				// (to encourage collisions)
				tests[i] = new StressTest( i, STRESS_TEST_SLEEP );
			}
			else
			{
				// Odd-numbered tests will have random timing
				tests[i] = new StressTest( i, (int) ( STRESS_TEST_SLEEP * new Random().nextFloat() * 2 ) );
			}
		}
		new MultiThreadedTestRunner( tests ).runTestRunnables();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final int threads;

	protected final int iterations;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final int STRESS_TEST_SLEEP = 5;

	private class StressTest extends TestRunnable
	{
		public StressTest( int index, int sleep )
		{
			this.index = index;
			this.sleep = sleep;
		}

		@Override
		public void runTest() throws Throwable
		{
			for( int i = 0; i < iterations; i++ )
			{
				test( index );
				if( iterations > 1 )
					Thread.sleep( sleep );
			}
		}

		private final int index;

		private final int sleep;
	}
}
