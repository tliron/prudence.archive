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

import org.restlet.Request;

/**
 * See {@link ConversationService#stop()}.
 * 
 * @author Tal Liron
 */
public class ConversationStoppedException extends RuntimeException
{
	//
	// Constants
	//

	/**
	 * Conversation stopped attribute for a {@link Request}.
	 */
	public static final String CONVERSATION_STOPPED_ATTRIBUTE = "com.threecrickets.prudence.service.ConversationStoppedException.conversationStopped";

	//
	// Static attributes
	//

	/**
	 * Whether the conversation was marked as stopped.
	 * 
	 * @param request
	 *        The request
	 * @return True if the conversation was marked as stopped
	 */
	public static boolean isConversationStopped( Request request )
	{
		Boolean conversationStopped = (Boolean) request.getAttributes().get( CONVERSATION_STOPPED_ATTRIBUTE );
		return conversationStopped == null ? false : conversationStopped;
	}

	/**
	 * Whether the conversation was marked as stopped.
	 * 
	 * @param request
	 *        The request
	 * @param conversationStopped
	 *        True if the conversation should be marked as stopped
	 */
	public static void setConversationStopped( Request request, boolean conversationStopped )
	{
		request.getAttributes().put( CONVERSATION_STOPPED_ATTRIBUTE, conversationStopped );
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        The request to mark as stopped
	 */
	public ConversationStoppedException( Request request )
	{
		super( "conversation.stop was called" );
		setConversationStopped( request, true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Serialized version.
	 */
	private static final long serialVersionUID = 1L;
}
