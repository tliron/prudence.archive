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

package com.threecrickets.prudence.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Template;
import org.restlet.util.Resolver;

import com.threecrickets.prudence.DelegatedCacheKeyPatternHandler;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.service.GeneratedTextResourceConversationService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * @author Tal Liron
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
	// Operations
	//

	public void callHandlers( Template template, Map<String, String> documentCacheKeyPatternHandlers )
	{
		Map<String, String> resourceCacheKeyPatternHandlers = resource.getAttributes().getCacheKeyPatternHandlers();

		if( ( ( resourceCacheKeyPatternHandlers == null ) || resourceCacheKeyPatternHandlers.isEmpty() ) && ( ( documentCacheKeyPatternHandlers == null ) || documentCacheKeyPatternHandlers.isEmpty() ) )
			return;

		List<String> variableNames = template.getVariableNames();

		// Merge all handlers
		Map<String, String> cacheKeyPatternHandlers = new HashMap<String, String>();
		if( resourceCacheKeyPatternHandlers != null )
			cacheKeyPatternHandlers.putAll( resourceCacheKeyPatternHandlers );
		if( documentCacheKeyPatternHandlers != null )
			cacheKeyPatternHandlers.putAll( documentCacheKeyPatternHandlers );

		// Group variables together per handler
		Map<String, Set<String>> delegatedHandlers = new HashMap<String, Set<String>>();
		for( Map.Entry<String, String> entry : cacheKeyPatternHandlers.entrySet() )
		{
			String name = entry.getKey();
			String documentName = entry.getValue();
			if( variableNames.contains( name ) )
			{
				Set<String> variables = delegatedHandlers.get( documentName );
				if( variables == null )
				{
					variables = new HashSet<String>();
					delegatedHandlers.put( documentName, variables );
				}
				variables.add( name );
			}
		}

		// Call handlers
		if( !delegatedHandlers.isEmpty() )
		{
			for( Map.Entry<String, Set<String>> entry : delegatedHandlers.entrySet() )
			{
				String documentName = entry.getKey();
				String[] variableNamesArray = entry.getValue().toArray( new String[] {} );
				
				DelegatedCacheKeyPatternHandler delegatedHandler = new DelegatedCacheKeyPatternHandler( documentName, resource.getContext() );
				delegatedHandler.handleCacheKeyPattern( variableNamesArray );
			}
		}
	}

	//
	// Resolver
	//

	@Override
	public Object resolve( String name )
	{
		if( name.equals( DOCUMENT_NAME_VARIABLE ) )
		{
			return documentDescriptor.getDefaultName();
		}
		else if( name.equals( APPLICATION_NAME_VARIABLE ) )
		{
			return resource.getApplication().getName();
		}
		else if( name.equals( PATH_TO_BASE_VARIABLE ) )
		{
			return conversationService.getPathToBase();
		}

		Object result = callResolver.resolve( name );
		if( result != null )
			return result;

		return null;
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
