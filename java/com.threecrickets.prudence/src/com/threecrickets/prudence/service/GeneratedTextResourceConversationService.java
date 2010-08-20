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
public class GeneratedTextResourceConversationService extends ConversationServiceBase<GeneratedTextResource>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param documentService
	 *        The document service
	 * @param entity
	 *        The entity or null
	 * @param variant
	 *        The variant or null
	 * @param defaultCharacterSet
	 *        The character set to use if unspecified by variant
	 */
	public GeneratedTextResourceConversationService( GeneratedTextResource resource, GeneratedTextResourceDocumentService documentService, Representation entity, Variant variant, CharacterSet defaultCharacterSet )
	{
		super( resource, entity, variant, defaultCharacterSet, resource.getFileUploadSizeThreshold(), resource.getFileUploadDirectory() );
		this.documentService = documentService;
	}

	//
	// Attributes
	//

	/**
	 * The cache key pattern for the current executable.
	 * 
	 * @return The cast cache key
	 * @see GeneratedTextResourceDocumentService#castCacheKeyPattern()
	 */
	public String getCacheKey()
	{
		return documentService.castCacheKeyPattern();
	}

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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final GeneratedTextResourceDocumentService documentService;
}
