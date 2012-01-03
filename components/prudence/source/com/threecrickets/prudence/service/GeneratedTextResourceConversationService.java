/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import org.restlet.data.CharacterSet;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.GeneratedTextResource;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 * @see GeneratedTextResource
 */
public class GeneratedTextResourceConversationService extends ResourceConversationServiceBase<GeneratedTextResource>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param entity
	 *        The entity or null
	 * @param preferences
	 *        The negotiated client preferences or null
	 * @param defaultCharacterSet
	 *        The character set to use if unspecified by variant
	 */
	public GeneratedTextResourceConversationService( GeneratedTextResource resource, Representation entity, Variant preferences, CharacterSet defaultCharacterSet )
	{
		super( resource, entity, preferences, defaultCharacterSet, GeneratedTextResource.SUPPORTED_ENCODINGS, resource.getAttributes().getFileUploadSizeThreshold(), resource.getAttributes().getFileUploadDirectory() );
	}

	//
	// Attributes
	//

	/**
	 * This boolean is true when the writer is in deferred mode.
	 * 
	 * @return True if in deferred mode
	 */
	public boolean isDeferred()
	{
		return isDeferred;
	}

	/**
	 * Identical to {@link #isDeferred()}. Supports scripting engines which
	 * don't know how to recognize the "is" getter notation, but can recognize
	 * the "get" notation.
	 * 
	 * @return True if in deferred mode
	 * @see #isDeferred()
	 */
	public boolean getIsDeferred()
	{
		return isDeferred();
	}

	//
	// Operations
	//

	/**
	 * Ask to defer this conversation.
	 * 
	 * @return True if deferred, false if already in deferred mode
	 */
	public boolean defer()
	{
		if( isDeferred )
			return false;

		defer = true;
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Flag to signify that we should enter deferred mode.
	 */
	protected boolean defer;

	/**
	 * This boolean is true when the writer is in deferred mode.
	 */
	protected boolean isDeferred;
}
