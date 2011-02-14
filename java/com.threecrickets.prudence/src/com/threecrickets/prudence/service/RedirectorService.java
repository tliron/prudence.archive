/**
 * Copyright 2009-2011 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import org.restlet.routing.Redirector;

/**
 * Redirector service exposed to executables.
 * 
 * @author Tal Liron
 */
public class RedirectorService
{
	//
	// Construction
	//

	public RedirectorService( int mode )
	{
		this.mode = mode;
	}

	//
	// Attributes
	//

	/**
	 * @return The mode
	 * @see #setMode(int)
	 */
	public int getMode()
	{
		return mode;
	}

	/**
	 * @param mode
	 *        The mode
	 * @see #getMode()
	 */
	public void setMode( int mode )
	{
		this.mode = mode;
	}

	/**
	 * Set a client redirect mode according to the status code returned to the
	 * client.
	 * 
	 * @param statusCode
	 *        The status code (301, 302, 303, or 307)
	 * @see #setMode(int)
	 */
	public void setStatusCode( int statusCode )
	{
		switch( statusCode )
		{
			case 301:
				mode = Redirector.MODE_CLIENT_PERMANENT;
				break;
			case 302:
				mode = Redirector.MODE_CLIENT_FOUND;
				break;
			case 303:
				mode = Redirector.MODE_CLIENT_SEE_OTHER;
				break;
			case 307:
				mode = Redirector.MODE_CLIENT_TEMPORARY;
				break;
			default:
				throw new IllegalArgumentException( "Unsupported status code: " + statusCode );
		}
	}

	/**
	 * Set a server redirect mode, either out-bound (true) or in-bound (false).
	 * 
	 * @param outbound
	 *        True if out-bound, false if in-bound
	 * @see #setMode(int)
	 */
	public void setServerOutbound( boolean outbound )
	{
		mode = outbound ? Redirector.MODE_SERVER_OUTBOUND : Redirector.MODE_SERVER_INBOUND;
	}

	/**
	 * @return The reference
	 * @see #setReference(String)
	 */
	public String getReference()
	{
		return reference;
	}

	/**
	 * @param reference
	 *        The reference
	 * @see #getReference()
	 */
	public void setReference( String reference )
	{
		this.reference = reference;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The redirect mode.
	 */
	private int mode;

	/**
	 * The target reference.
	 */
	private String reference;
}
