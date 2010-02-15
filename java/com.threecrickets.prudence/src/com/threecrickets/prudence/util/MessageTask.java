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

package com.threecrickets.prudence.util;

import org.restlet.Context;

/**
 * @author Tal Liron
 */
public class MessageTask implements Runnable
{
	//
	// Construction
	//

	/**
	 * @param context
	 * @param message
	 */
	public MessageTask( Context context, String message )
	{
		this.context = context;
		this.message = message;
	}

	//
	// Runnable
	//

	public void run()
	{
		context.getLogger().info( message );
		System.out.println( message );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Context context;

	private final String message;
}
