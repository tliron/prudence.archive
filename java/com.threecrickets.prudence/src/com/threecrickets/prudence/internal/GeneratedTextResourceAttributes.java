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

import java.io.File;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;

public class GeneratedTextResourceAttributes extends ResourceContextualAttributes<GeneratedTextResource>
{
	//
	// Resource
	//

	public GeneratedTextResourceAttributes( GeneratedTextResource resource )
	{
		super( resource );
	}

	//
	// Attributes
	//

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "index".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultName</code> in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultIncludedName()
	{
		if( defaultIncludedName == null )
		{
			defaultIncludedName = (String) getAttributes().get( prefix + ".defaultIncludedName" );

			if( defaultIncludedName == null )
				defaultIncludedName = "index";
		}

		return defaultIncludedName;
	}

	/**
	 * The name of the global variable with which to access the conversation
	 * service. Defaults to "conversation".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>conversationServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The conversation service name
	 */
	public String getConversationServiceName()
	{
		if( conversationServiceName == null )
		{
			conversationServiceName = (String) getAttributes().get( prefix + ".conversationServiceName" );

			if( conversationServiceName == null )
				conversationServiceName = "conversation";
		}

		return conversationServiceName;
	}

	/**
	 * The default cache key pattern to use if the executable doesn't specify
	 * one. Defaults to "{ri}|{dn}|{ptb}".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultCacheKeyPattern</code> in the application's {@link Context}.
	 * 
	 * @return The default cache key
	 */
	public String getDefaultCacheKeyPattern()
	{
		if( defaultCacheKeyPattern == null )
		{
			defaultCacheKeyPattern = (String) getAttributes().get( prefix + ".defaultCacheKeyPattern" );

			if( defaultCacheKeyPattern == null )
				defaultCacheKeyPattern = "{ri}|{dn}|{ptb}";
		}

		return defaultCacheKeyPattern;
	}

	/**
	 * The cache key pattern handlers.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>cacheKeyPatternHandlers</code> in the application's {@link Context}.
	 * 
	 * @return The cache key pattern handlers or null
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, String> getCacheKeyPatternHandlers()
	{
		if( cacheKeyPatternHandlers == null )
			cacheKeyPatternHandlers = (ConcurrentMap<String, String>) getAttributes().get( prefix + ".cacheKeyPatternHandlers" );

		return cacheKeyPatternHandlers;
	}

	/**
	 * Executables might use this directory for including fragments. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../fragments/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>fragmentDirectory</code> in the application's {@link Context}.
	 * 
	 * @return The fragment directory or null
	 */
	public File getFragmentDirectory()
	{
		if( fragmentDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			fragmentDirectory = (File) attributes.get( prefix + ".fragmentDirectory" );

			if( fragmentDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					fragmentDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../fragments/" );

					File existing = (File) attributes.putIfAbsent( prefix + ".fragmentDirectory", fragmentDirectory );
					if( existing != null )
						fragmentDirectory = existing;
				}
			}
		}

		return fragmentDirectory;
	}

	/**
	 * Whether or not to negotiate encoding by default. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>negotiateEncoding</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isNegotiateEncoding()
	{
		if( negotiateEncoding == null )
		{
			negotiateEncoding = (Boolean) getAttributes().get( prefix + ".negotiateEncoding" );

			if( negotiateEncoding == null )
				negotiateEncoding = true;
		}

		return negotiateEncoding;
	}

	/**
	 * Whether or not to send information to the client about cache expiration.
	 * Defaults to {@link #CLIENT_CACHING_MODE_CONDITIONAL}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>clientCachingMode</code> in the application's {@link Context}.
	 * 
	 * @return The client caching mode
	 */
	public int getClientCachingMode()
	{
		if( clientCachingMode == null )
		{
			Number number = (Number) getAttributes().get( prefix + ".clientCachingMode" );

			if( number != null )
				clientCachingMode = number.intValue();

			if( clientCachingMode == null )
				clientCachingMode = GeneratedTextResource.CLIENT_CACHING_MODE_CONDITIONAL;
		}

		return clientCachingMode;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private volatile String defaultIncludedName;

	/**
	 * The name of the global variable with which to access the conversation
	 * service.
	 */
	private volatile String conversationServiceName;

	/**
	 * The default cache key pattern to use if the executable doesn't specify
	 * one.
	 */
	private volatile String defaultCacheKeyPattern;

	/**
	 * The cache key pattern handlers.
	 */
	private volatile ConcurrentMap<String, String> cacheKeyPatternHandlers;

	/**
	 * Executables might use this directory for including fragments.
	 */
	private volatile File fragmentDirectory;

	/**
	 * Whether or not to negotiate encoding by default.
	 */
	private volatile Boolean negotiateEncoding;

	/**
	 * Whether or not to send information to the client about cache expiration.
	 */
	private volatile Integer clientCachingMode;
}
