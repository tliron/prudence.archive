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

/**
 * See {@link ConversationService#stop()}.
 * 
 * @author Tal Liron
 */
public class ConversationStoppedException extends RuntimeException
{
	//
	// Construction
	//

	public ConversationStoppedException()
	{
		super( "conversation.stop was called" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Serialized version.
	 */
	private static final long serialVersionUID = 1L;
}
