/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.test;

import com.threecrickets.prudence.test.internal.DistributionTest;
import com.threecrickets.prudence.test.internal.TestOK;
import com.threecrickets.prudence.test.internal.TestRedirected;

/**
 * @author Tal Liron
 */
public class Python extends DistributionTest
{
	//
	// Construction
	//

	public Python()
	{
		super( "python", true );
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
		new TestOK( "/prudence-test/test/jython/?id=hello" ), new TestOK( "/prudence-test/test/velocity/?id=hello" ),
		// /resources/
		new TestOK( "/prudence-test/data/jython/" )
	};
}
