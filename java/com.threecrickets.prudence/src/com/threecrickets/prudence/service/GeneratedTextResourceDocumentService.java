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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.routing.Template;
import org.restlet.routing.Variable;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.GeneratedTextDeferredRepresentation;
import com.threecrickets.prudence.util.CaptiveRedirector;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 */
public class GeneratedTextResourceDocumentService extends DocumentServiceBase<GeneratedTextResource>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param executionContext
	 *        The execution context
	 * @param conversationService
	 *        The exposed conversation
	 */
	public GeneratedTextResourceDocumentService( GeneratedTextResource resource, ExecutionContext executionContext, Representation entity, Variant variant )
	{
		super( resource, resource.getDocumentSource() );
		this.executionContext = executionContext;
		this.conversationService = new GeneratedTextResourceConversationService( resource, entity, variant, resource.getDefaultCharacterSet() );
	}

	/**
	 * Construction by cloning, with new execution context (for deferred
	 * execution).
	 * 
	 * @param documentService
	 *        The document service to clone
	 */
	public GeneratedTextResourceDocumentService( GeneratedTextResourceDocumentService documentService )
	{
		this( documentService.resource, new ExecutionContext(), documentService.conversationService.getEntity(), documentService.conversationService.getVariant() );

		currentExecutable = documentService.currentExecutable;
		conversationService.isDeferred = true;

		// Initialize execution context
		executionContext.getServices().put( resource.getDocumentServiceName(), this );
		executionContext.getServices().put( resource.getApplicationServiceName(), exposedApplication );
		executionContext.getServices().put( resource.getConversationServiceName(), conversationService );
		File libraryDirectory = resource.getLibraryDirectory();
		if( libraryDirectory != null )
			executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
	}

	//
	// Attributes
	//

	/**
	 * @return The cache duration in milliseconds
	 * @see #setCacheDuration(long)
	 */
	public long getCacheDuration()
	{
		Long cacheDuration = (Long) currentExecutable.getAttributes().get( CACHE_DURATION_ATTRIBUTE );
		return cacheDuration == null ? 0 : cacheDuration;
	}

	/**
	 * @param cacheDuration
	 *        The cache duration in milliseconds
	 * @see #getCacheDuration()
	 */
	public void setCacheDuration( long cacheDuration )
	{
		currentExecutable.getAttributes().put( CACHE_DURATION_ATTRIBUTE, cacheDuration );
	}

	/**
	 * @return The cache key pattern
	 * @see #setCacheKey(String)
	 */
	public String getCacheKey()
	{
		return (String) currentExecutable.getAttributes().get( CACHE_KEY_ATTRIBUTE );
	}

	/**
	 * @param cacheKey
	 *        The cache key pattern
	 * @see #getCacheKey()
	 */
	public void setCacheKey( String cacheKey )
	{
		currentExecutable.getAttributes().put( CACHE_KEY_ATTRIBUTE, cacheKey );
	}

	/**
	 * @return The cache tags
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getCacheTags()
	{
		Set<String> cacheTags = (Set<String>) currentExecutable.getAttributes().get( CACHE_TAGS_ATTRIBUTE );
		if( cacheTags == null )
		{
			cacheTags = new HashSet<String>();
			Set<String> existing = (Set<String>) currentExecutable.getAttributes().putIfAbsent( CACHE_TAGS_ATTRIBUTE, cacheTags );
			if( existing != null )
				cacheTags = existing;

		}
		return cacheTags;
	}

	/**
	 * The cache.
	 * 
	 * @return The cache
	 */
	public Cache getCache()
	{
		return resource.getCache();
	}

	//
	// Operations
	//

	/**
	 * Includes a text document into the current location. The document may be a
	 * "text-with-scriptlets" executable, in which case its output could be
	 * dynamically generated.
	 * 
	 * @param documentName
	 *        The document name
	 * @return A representation of the document's output
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public Representation include( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = resource.validateDocumentName( documentName );

		DocumentDescriptor<Executable> documentDescriptor;
		try
		{
			documentDescriptor = Executable.createOnce( documentName, resource.getDocumentSource(), true, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		}
		catch( DocumentNotFoundException x )
		{
			// Try the fragment directory
			File fragmentDirectory = resource.getFragmentDirectoryRelative();
			if( fragmentDirectory != null )
				documentDescriptor = Executable.createOnce( fragmentDirectory.getPath() + "/" + documentName, resource.getDocumentSource(), true, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource
					.isPrepare() );
			else
				throw x;
		}

		if( conversationService.getMediaType() == null )
			// Set initial media type according to the document's tag
			conversationService.setMediaTypeExtension( documentDescriptor.getTag() );

		return execute( documentDescriptor.getDocument() );
	}

	/**
	 * Executes a source code document. The language of the source code will be
	 * determined by the document tag, which is usually the filename extension.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	@Override
	public Representation execute( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = resource.validateDocumentName( documentName );

		Executable executable;
		try
		{
			executable = Executable.createOnce( documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() ).getDocument();
		}
		catch( DocumentNotFoundException x )
		{
			File libraryDirectory = resource.getLibraryDirectoryRelative();
			if( libraryDirectory != null )
				// Try the library directory
				executable = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(),
					resource.isPrepare() ).getDocument();
			else
				throw x;
		}

		return execute( executable );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The exposed conversation.
	 */
	protected final GeneratedTextResourceConversationService conversationService;

	/**
	 * The exposed application.
	 */
	protected final ApplicationService exposedApplication = new ApplicationService();

	/**
	 * The execution context.
	 */
	protected final ExecutionContext executionContext;

	/**
	 * The currently executing executable.
	 */
	protected Executable currentExecutable;

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	protected Writer writer;

	/**
	 * Buffer used for caching mode.
	 */
	protected StringBuffer writerBuffer;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String DOCUMENT_NAME_VARIABLE = "dn";

	private static final String CACHE_DURATION_ATTRIBUTE = "prudence.cacheDuration";

	private static final String CACHE_KEY_ATTRIBUTE = "prudence.cacheKey";

	private static final String CACHE_TAGS_ATTRIBUTE = "prudence.cacheTags";

	/**
	 * @return The cache key for the executable
	 */
	private String castCacheKeyPattern()
	{
		String cacheKey = getCacheKey();
		if( cacheKey == null )
			return null;
		else
		{
			Template template = new Template( cacheKey );

			// Our additional template variable: {dn}
			template.getVariables().put( DOCUMENT_NAME_VARIABLE, new Variable( Variable.TYPE_ALL, currentExecutable.getDocumentName(), true, true ) );

			// Use captive reference as the resource reference
			Reference captiveReference = CaptiveRedirector.getCaptiveReference( resource.getRequest() );
			Reference resourceReference = resource.getRequest().getResourceRef();
			if( captiveReference != null )
				resource.getRequest().setResourceRef( captiveReference );

			String cast = template.format( resource.getRequest(), resource.getResponse() );

			if( captiveReference != null )
				resource.getRequest().setResourceRef( resourceReference );

			return cast;
		}
	}

	/**
	 * @return The cache expiration timestamp for the executable
	 */
	private long getExpiration()
	{
		long cacheDuration = getCacheDuration();
		if( cacheDuration <= 0 )
			return 0;
		else
			return currentExecutable.getLastExecutedTimestamp() + cacheDuration;
	}

	/**
	 * The actual execution of an executable.
	 * 
	 * @return A representation, either generated by the executable or fetched
	 *         from the cache
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	private Representation execute( Executable executable ) throws IOException, ParsingException, ExecutionException
	{
		Executable previousExecutable = currentExecutable;
		currentExecutable = executable;

		// Optimized handling for pure text
		String pureText = currentExecutable.getAsPureLiteral();
		if( pureText != null )
		{
			// We want to write this, too, for includes
			if( writer != null )
				writer.write( pureText );

			return new CacheEntry( pureText, conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), getExpiration() ).represent();
		}

		int startPosition = 0;

		// Make sure we have a valid writer for caching mode
		if( !conversationService.isDeferred )
		{
			if( writer == null )
			{
				StringWriter stringWriter = new StringWriter();
				writerBuffer = stringWriter.getBuffer();
				writer = new BufferedWriter( stringWriter );
			}
			else
			{
				writer.flush();
				startPosition = writerBuffer.length();
			}

			// Attempt to use cache
			String cacheKey = castCacheKeyPattern();
			if( cacheKey != null )
			{
				Cache cache = resource.getCache();
				if( cache != null )
				{
					CacheEntry cacheEntry = cache.fetch( cacheKey );
					if( cacheEntry != null )
					{
						// We want to write this, too, for includes
						if( writer != null )
							writer.write( cacheEntry.getString() );

						return cacheEntry.represent();
					}
				}
			}
		}

		setCacheDuration( 0 );
		setCacheKey( resource.getDefaultCacheKey() );
		getCacheTags().clear();

		try
		{
			executionContext.setWriter( writer );
			executionContext.getServices().put( resource.getDocumentServiceName(), this );
			executionContext.getServices().put( resource.getApplicationServiceName(), exposedApplication );
			executionContext.getServices().put( resource.getConversationServiceName(), conversationService );

			// Execute!
			executable.execute( executionContext, this, resource.getExecutionController() );
			currentExecutable = executable;

			// Did the executable ask us to defer?
			if( conversationService.defer )
			{
				conversationService.defer = false;

				// Note that this will cause the executable to execute again!
				GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this );
				return new GeneratedTextDeferredRepresentation( resource, currentExecutable, documentService.executionContext, documentService, documentService.conversationService );
			}

			if( conversationService.isDeferred )
			{
				// Nothing to return in deferred mode
				return null;
			}
			else
			{
				writer.flush();

				// Get the buffer from when we executed the executable
				CacheEntry cacheEntry = new CacheEntry( writerBuffer.substring( startPosition ), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(),
					getExpiration() );

				// Cache if enabled
				String cacheKey = castCacheKeyPattern();
				Collection<String> cacheTags = getCacheTags();
				if( ( cacheKey != null ) && ( cacheEntry.getExpirationDate() != null ) )
				{
					Cache cache = resource.getCache();
					if( cache != null )
						cache.store( cacheKey, cacheTags, cacheEntry );
				}

				// Return a representation of the entire buffer
				if( startPosition == 0 )
					return cacheEntry.represent();
				else
					return new CacheEntry( writerBuffer.toString(), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), getExpiration() ).represent();
			}
		}
		catch( ExecutionException x )
		{
			// Did the executable ask us to defer?
			if( conversationService.defer )
			{
				// Note that we will allow exceptions in an executable that ask
				// us to defer! In fact, throwing an exception is a good way for
				// the executable to signal that it's done and is ready to
				// defer.

				conversationService.defer = false;

				// Note that this will cause the executable to run again!
				GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this );
				return new GeneratedTextDeferredRepresentation( resource, currentExecutable, documentService.executionContext, documentService, documentService.conversationService );
			}
			else
				throw x;
		}
		finally
		{
			writer.flush();
			executionContext.getErrorWriterOrDefault().flush();

			currentExecutable = previousExecutable;
		}
	}
}