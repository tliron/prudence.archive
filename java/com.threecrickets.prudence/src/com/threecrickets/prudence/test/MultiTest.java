package com.threecrickets.prudence.test;

import java.util.Random;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.junit.Test;

public abstract class MultiTest
{
	public MultiTest( int threads, int iterations )
	{
		this.threads = threads;
		this.iterations = iterations;
	}

	public abstract void test( int index );

	@Test
	public void singleThread() throws Throwable
	{
		test( 0 );
	}

	@Test
	public void multiThreaded() throws Throwable
	{
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

	public StressTest newStressTest( int index )
	{
		return new StressTest( index, STRESS_TEST_SLEEP );
	}

	public class StressTest extends TestRunnable
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final int STRESS_TEST_SLEEP = 5;

	private final int threads;

	private final int iterations;
}
