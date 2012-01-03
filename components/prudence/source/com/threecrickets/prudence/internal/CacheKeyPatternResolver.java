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

package com.threecrickets.prudence.internal;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.util.Resolver;

import com.threecrickets.prudence.DelegatedCacheKeyPatternHandler;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.service.GeneratedTextResourceConversationService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * Resolves a few special Prudence variables.
 * 
 * @author Tal Liron
 * @see DelegatedCacheKeyPatternHandler
 */
public class CacheKeyPatternResolver extends Resolver<Object>
{
	//
	// Construction
	//

	public CacheKeyPatternResolver( DocumentDescriptor<Executable> documentDescriptor, GeneratedTextResource resource, GeneratedTextResourceConversationService conversationService )
	{
		this( documentDescriptor, resource, conversationService, resource.getRequest(), resource.getResponse() );
	}

	public CacheKeyPatternResolver( DocumentDescriptor<Executable> documentDescriptor, GeneratedTextResource resource, GeneratedTextResourceConversationService conversationService, Request request, Response response )
	{
		this( documentDescriptor, resource, conversationService, Resolver.createResolver( request, response ) );
	}

	public CacheKeyPatternResolver( DocumentDescriptor<Executable> documentDescriptor, GeneratedTextResource resource, GeneratedTextResourceConversationService conversationService, Resolver<?> callResolver )
	{
		this.documentDescriptor = documentDescriptor;
		this.resource = resource;
		this.conversationService = conversationService;
		this.callResolver = callResolver;
	}

	//
	// Resolver
	//

	@Override
	public Object resolve( String name )
	{
		if( name.equals( DOCUMENT_NAME_VARIABLE ) )
			return documentDescriptor.getDefaultName();
		else if( name.equals( APPLICATION_NAME_VARIABLE ) )
			return resource.getApplication().getName();
		else if( name.equals( PATH_TO_BASE_VARIABLE ) )
			return conversationService.getPathToBase();

		return callResolver.resolve( name );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String DOCUMENT_NAME_VARIABLE = "dn";

	private static final String APPLICATION_NAME_VARIABLE = "an";

	private static final String PATH_TO_BASE_VARIABLE = "ptb";

	private final DocumentDescriptor<Executable> documentDescriptor;

	private final GeneratedTextResource resource;

	private final GeneratedTextResourceConversationService conversationService;

	private final Resolver<?> callResolver;
}
