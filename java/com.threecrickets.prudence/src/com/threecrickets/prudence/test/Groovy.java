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

import com.threecrickets.prudence.test.internal.Distribution;
import com.threecrickets.prudence.test.internal.TestOK;
import com.threecrickets.prudence.test.internal.TestRedirected;

/**
 * @author Tal Liron
 */
public class Groovy extends Distribution
{
	//
	// Construction
	//

	public Groovy()
	{
		super( "groovy" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected Runnable[] getPrudenceTests()
	{
		return prudenceTests;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Runnable[] prudenceTests = new Runnable[]
	{
		new TestOK( "/prudence-test/" ), new TestRedirected( "/prudence-test" ),
		// /web/static/
		new TestOK( "/prudence-test/style/soft-cricket.css" ),
		// /web/dynamic/
		new TestOK( "/prudence-test/test/groovy/?id=hello" ),
		// /resources/
		new TestOK( "/prudence-test/data/groovy/" )
	};
}
