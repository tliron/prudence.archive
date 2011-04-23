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

package com.threecrickets.prudence.internal.attributes;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;

import com.threecrickets.prudence.DelegatedScriptletPlugin;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.ScriptletPlugin;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public class GeneratedTextResourceAttributes extends ResourceContextualAttributes
{
	//
	// Resource
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 */
	public GeneratedTextResourceAttributes( GeneratedTextResource resource )
	{
		super( resource );
	}

	//
	// Attributes
	//

	@Override
	public File getFileUploadDirectory()
	{
		if( fileUploadDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			fileUploadDirectory = (File) attributes.get( prefix + ".fileUploadDirectory" );

			if( fileUploadDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					fileUploadDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../../uploads/" );

					File existing = (File) attributes.putIfAbsent( prefix + ".fileUploadDirectory", fileUploadDirectory );
					if( existing != null )
						fileUploadDirectory = existing;
				}
			}
		}

		return fileUploadDirectory;
	}

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

	/**
	 * The scriptlet plugins to use during parsing.
	 * 
	 * @return The scriptlet plugins or null
	 * @see DelegatedScriptletPlugin
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, ScriptletPlugin> getScriptletPlugins()
	{
		if( scriptletPlugins == null )
		{
			scriptletPlugins = new ConcurrentHashMap<String, ScriptletPlugin>();

			Map<String, Object> potentialScriptletPlugins = (ConcurrentMap<String, Object>) getAttributes().get( prefix + ".scriptletPlugins" );
			if( potentialScriptletPlugins != null )
			{
				for( Map.Entry<String, Object> entry : potentialScriptletPlugins.entrySet() )
				{
					Object scriptletPlugin = entry.getValue();
					if( scriptletPlugin instanceof ScriptletPlugin )
						scriptletPlugins.put( entry.getKey(), (ScriptletPlugin) scriptletPlugin );
					else
						// Create delegated scriptlet plugin
						scriptletPlugins.put( entry.getKey(), new DelegatedScriptletPlugin( scriptletPlugin.toString(), resource.getContext() ) );
				}
			}
		}

		return scriptletPlugins;
	}

	//
	// DocumentExecutionAttributes
	//

	@Override
	public DocumentDescriptor<Executable> createOnce( String documentName, boolean isTextWithScriptlets, boolean includeMainSource, boolean includeExtraSources, boolean includeLibrarySources ) throws ParsingException,
		DocumentException
	{
		ParsingContext parsingContext = new ParsingContext();
		parsingContext.setLanguageManager( getLanguageManager() );
		parsingContext.setDefaultLanguageTag( getDefaultLanguageTag() );
		parsingContext.setPrepare( isPrepare() );
		if( includeMainSource )
			parsingContext.setDocumentSource( getDocumentSource() );

		if( isTextWithScriptlets )
		{
			Map<String, ScriptletPlugin> scriptletPlugins = getScriptletPlugins();
			if( scriptletPlugins != null )
				parsingContext.getScriptletPlugins().putAll( scriptletPlugins );
		}

		Iterator<DocumentSource<Executable>> iterator = null;
		while( true )
		{
			try
			{
				if( parsingContext.getDocumentSource() == null )
					throw new DocumentNotFoundException( documentName );

				return Executable.createOnce( documentName, isTextWithScriptlets, parsingContext );
			}
			catch( DocumentNotFoundException x )
			{
				if( ( ( iterator == null ) || !iterator.hasNext() ) && includeExtraSources )
				{
					Iterable<DocumentSource<Executable>> sources = getExtraDocumentSources();
					iterator = sources != null ? sources.iterator() : null;
					includeExtraSources = false;
				}

				if( ( ( iterator == null ) || !iterator.hasNext() ) && includeLibrarySources )
				{
					Iterable<DocumentSource<Executable>> sources = getLibraryDocumentSources();
					iterator = sources != null ? sources.iterator() : null;
					includeLibrarySources = false;
				}

				if( ( iterator == null ) || !iterator.hasNext() )
					throw new DocumentNotFoundException( documentName );

				parsingContext.setDocumentSource( iterator.next() );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private String defaultIncludedName;

	/**
	 * The name of the global variable with which to access the conversation
	 * service.
	 */
	private String conversationServiceName;

	/**
	 * The default cache key pattern to use if the executable doesn't specify
	 * one.
	 */
	private String defaultCacheKeyPattern;

	/**
	 * The cache key pattern handlers.
	 */
	private ConcurrentMap<String, String> cacheKeyPatternHandlers;

	/**
	 * Whether or not to negotiate encoding by default.
	 */
	private Boolean negotiateEncoding;

	/**
	 * Whether or not to send information to the client about cache expiration.
	 */
	private Integer clientCachingMode;

	/**
	 * The scriptlet plugins used during parsing.
	 */
	private ConcurrentMap<String, ScriptletPlugin> scriptletPlugins;
}
