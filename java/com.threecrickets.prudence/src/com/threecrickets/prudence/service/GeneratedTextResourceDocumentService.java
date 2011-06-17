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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.routing.Template;

import com.threecrickets.prudence.DelegatedCacheKeyPatternHandler;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.CacheKeyPatternResolver;
import com.threecrickets.prudence.internal.GeneratedTextDeferredRepresentation;
import com.threecrickets.prudence.internal.attributes.GeneratedTextResourceAttributes;
import com.threecrickets.prudence.util.CapturingRedirector;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 */
public class GeneratedTextResourceDocumentService extends ResourceDocumentServiceBase<GeneratedTextResource, GeneratedTextResourceAttributes>
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
	 * @param entity
	 *        The entity
	 * @param preferences
	 *        The negotiated client preferences or null
	 */
	public GeneratedTextResourceDocumentService( GeneratedTextResource resource, ExecutionContext executionContext, Representation entity, Variant preferences )
	{
		super( resource, resource.getAttributes() );
		this.executionContext = executionContext;
		conversationService = new GeneratedTextResourceConversationService( resource, entity, preferences, attributes.getDefaultCharacterSet() );
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
		super( documentService.resource, documentService.attributes );
		conversationService = documentService.conversationService;
		pushDocumentDescriptor( documentService.getCurrentDocumentDescriptor() );
		executionContext = new ExecutionContext();
		attributes.addLibraryLocations( executionContext );

		// Initialize execution context
		executionContext.getServices().put( attributes.getDocumentServiceName(), this );
		executionContext.getServices().put( attributes.getApplicationServiceName(), applicationService );
		executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

		conversationService.isDeferred = true;
	}

	//
	// Attributes
	//

	/**
	 * The cache duration.
	 * 
	 * @return The cache duration in milliseconds
	 * @see #setCacheDuration(long)
	 */
	public long getCacheDuration()
	{
		Long cacheDuration = (Long) getCurrentDocumentDescriptor().getDocument().getAttributes().get( CACHE_DURATION_ATTRIBUTE );
		return cacheDuration == null ? 0 : cacheDuration;
	}

	/**
	 * @param cacheDuration
	 *        The cache duration in milliseconds
	 * @see #getCacheDuration()
	 */
	public void setCacheDuration( long cacheDuration )
	{
		getCurrentDocumentDescriptor().getDocument().getAttributes().put( CACHE_DURATION_ATTRIBUTE, cacheDuration );
	}

	/**
	 * The cache key pattern.
	 * 
	 * @return The cache key pattern
	 * @see #setCacheKeyPattern(String)
	 */
	public String getCacheKeyPattern()
	{
		return getCacheKeyPattern( getCurrentDocumentDescriptor().getDocument() );
	}

	/**
	 * @param cacheKeyPattern
	 *        The cache key pattern
	 * @see #getCacheKeyPattern()
	 */
	public void setCacheKeyPattern( String cacheKeyPattern )
	{
		getCurrentDocumentDescriptor().getDocument().getAttributes().put( CACHE_KEY_PATTERN_ATTRIBUTE, cacheKeyPattern );
	}

	/**
	 * The cache key pattern handlers.
	 * 
	 * @return The cache key pattern handlers
	 */
	public ConcurrentMap<String, String> getCacheKeyPatternHandlers()
	{
		return getCacheKeyPatternHandlers( getCurrentDocumentDescriptor().getDocument(), true );
	}

	/**
	 * Casts the cache key pattern for the current executable and encoding.
	 * 
	 * @return The cache key or null
	 */
	public String getCacheKey()
	{
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		return getCacheKeyForEncoding( castCacheKey( currentDocumentDescriptor ), getEncoding( currentDocumentDescriptor.getDocument() ) );
	}

	/**
	 * @return The cache tags
	 */
	public Set<String> getCacheTags()
	{
		return getCacheTags( getCurrentDocumentDescriptor().getDocument(), true );
	}

	/**
	 * The cache.
	 * 
	 * @return The cache
	 */
	public Cache getCache()
	{
		return attributes.getCache();
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
		return include( documentName, false );
	}

	/**
	 * Includes a text document into the current location. The document may be a
	 * "text-with-scriptlets" executable, in which case its output could be
	 * dynamically generated.
	 * 
	 * @param documentName
	 *        The document name
	 * @param isPassThrough
	 *        Whether to force looking for the document in the extra document
	 *        sources (otherwise is only allowed for non-initial documents)
	 * @return A representation of the document's output
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public Representation include( String documentName, boolean isPassThrough ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = attributes.validateDocumentName( documentName, attributes.getDefaultIncludedName() );

		// This will be null if we're the initial document
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();

		DocumentDescriptor<Executable> documentDescriptor = null;

		// For initial documents, see if a document descriptor is cached for us
		// in the request
		if( currentDocumentDescriptor == null )
		{
			@SuppressWarnings("unchecked")
			DocumentDescriptor<Executable> existing = (DocumentDescriptor<Executable>) resource.getRequest().getAttributes().remove( DOCUMENT_DESCRIPTOR_ATTRIBUTE );
			documentDescriptor = existing;
		}

		if( documentDescriptor == null )
			documentDescriptor = attributes.createDocumentOnce( documentName, true, true, isPassThrough || ( currentDocumentDescriptor != null ), false );

		if( currentDocumentDescriptor == null )
		{
			// Set initial media type according to the document's tag
			if( conversationService.getMediaType() == null )
				conversationService.setMediaTypeExtension( documentDescriptor.getTag() );

			// Set the initial document encoding to that of the conversation
			Encoding encoding = conversationService.getEncoding();
			setEncoding( documentDescriptor.getDocument(), encoding );
		}
		else
		{
			// Add dependency
			currentDocumentDescriptor.getDependencies().add( documentDescriptor );
		}

		// Execute
		pushDocumentDescriptor( documentDescriptor );
		try
		{
			return generateText( documentDescriptor );
		}
		finally
		{
			popDocumentDescriptor();
		}
	}

	/**
	 * Gets the cache entry for a document, if it exists and is valid.
	 * 
	 * @param documentName
	 *        The document name
	 * @param isPassThrough
	 *        Whether to force looking for the document in the extra document
	 *        sources
	 * @return The cache entry
	 * @throws ParsingException
	 * @throws DocumentException
	 */
	public CacheEntry getCacheEntry( String documentName, boolean isPassThrough ) throws ParsingException, DocumentException
	{
		documentName = attributes.validateDocumentName( documentName, attributes.getDefaultIncludedName() );

		DocumentDescriptor<Executable> documentDescriptor = attributes.createDocumentOnce( documentName, true, true, isPassThrough, false );

		// Cache the document descriptor in the request
		resource.getRequest().getAttributes().put( DOCUMENT_DESCRIPTOR_ATTRIBUTE, documentDescriptor );

		// Set initial media type according to the document's tag
		if( conversationService.getMediaType() == null )
			conversationService.setMediaTypeExtension( documentDescriptor.getTag() );

		// Set the initial document encoding to that of the conversation
		Encoding encoding = conversationService.getEncoding();
		setEncoding( documentDescriptor.getDocument(), encoding );

		String cacheKey = castCacheKey( documentDescriptor );
		if( cacheKey != null )
		{
			Cache cache = attributes.getCache();
			if( cache != null )
			{
				// Try cache key for encoding first
				String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, getEncoding( documentDescriptor.getDocument() ) );
				CacheEntry cacheEntry = cache.fetch( cacheKeyForEncoding );
				if( cacheEntry == null )
					cacheEntry = cache.fetch( cacheKey );

				if( cacheEntry != null )
				{
					// Make sure the document is not newer than the cache
					// entry
					if( documentDescriptor.getDocument().getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() )
					{
						// Cache the cache entry in the request
						resource.getRequest().getAttributes().put( CACHE_KEY_ATTRIBUTE, cacheKey );
						resource.getRequest().getAttributes().put( CACHE_ENTRY_ATTRIBUTE, cacheEntry );

						return cacheEntry;
					}
				}
			}
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		documentName = attributes.validateDocumentName( documentName );
		return attributes.createDocumentOnce( documentName, false, false, false, true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Encoding attribute for an {@link Executable}.
	 */
	private static final String ENCODING_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.encoding";

	/**
	 * Cache duration attribute for an {@link Executable}.
	 */
	private static final String CACHE_DURATION_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheDuration";

	/**
	 * Cache key pattern attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_PATTERN_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheKeyPattern";

	/**
	 * Cache key pattern handlers attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_PATTERN_HANDLERS_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers";

	/**
	 * Cache tags attribute for an {@link Executable}.
	 */
	private static final String CACHE_TAGS_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheTags";

	/**
	 * Document descriptor attribute for a {@link Request}.
	 */
	private static final String DOCUMENT_DESCRIPTOR_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.documentDescriptor";

	/**
	 * Cache key attribute for a {@link Request}.
	 */
	private static final String CACHE_KEY_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheKey";

	/**
	 * Cache entry attribute for a {@link Request}.
	 */
	private static final String CACHE_ENTRY_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheEntry";

	/**
	 * The conversation service.
	 */
	private final GeneratedTextResourceConversationService conversationService;

	/**
	 * The application service.
	 */
	private final ApplicationService applicationService = new ApplicationService();

	/**
	 * The execution context.
	 */
	private final ExecutionContext executionContext;

	/**
	 * Buffer used for caching.
	 */
	private StringBuffer writerBuffer;

	/**
	 * The encoding for the executable (different from the encoding for the
	 * conversation).
	 * 
	 * @param executable
	 *        The executable
	 * @return The encoding or null
	 */
	private static Encoding getEncoding( Executable executable )
	{
		return (Encoding) executable.getAttributes().get( ENCODING_ATTRIBUTE );
	}

	/**
	 * The encoding for the executable (different from the encoding for the
	 * conversation).
	 * 
	 * @param executable
	 *        The executable
	 * @param encoding
	 *        The encoding
	 */
	private static void setEncoding( Executable executable, Encoding encoding )
	{
		if( ( encoding == null ) || Encoding.IDENTITY.equals( encoding ) )
			executable.getAttributes().remove( ENCODING_ATTRIBUTE );
		else
			executable.getAttributes().put( ENCODING_ATTRIBUTE, encoding );
	}

	/**
	 * The cache key pattern.
	 * 
	 * @param executable
	 *        The executable
	 * @return The cache key pattern
	 */
	private static String getCacheKeyPattern( Executable executable )
	{
		return (String) executable.getAttributes().get( CACHE_KEY_PATTERN_ATTRIBUTE );
	}

	/**
	 * @param executable
	 *        The executable
	 * @param create
	 *        Whether to create a handler map if it doesn't exist
	 * @return The handler map or null
	 */
	private static ConcurrentMap<String, String> getCacheKeyPatternHandlers( Executable executable, boolean create )
	{
		@SuppressWarnings("unchecked")
		ConcurrentMap<String, String> handlers = (ConcurrentMap<String, String>) executable.getAttributes().get( CACHE_KEY_PATTERN_HANDLERS_ATTRIBUTE );
		if( handlers == null && create )
		{
			handlers = new ConcurrentHashMap<String, String>();
			@SuppressWarnings("unchecked")
			ConcurrentMap<String, String> existing = (ConcurrentMap<String, String>) executable.getAttributes().putIfAbsent( CACHE_KEY_PATTERN_HANDLERS_ATTRIBUTE, handlers );
			if( existing != null )
				handlers = existing;
		}

		return handlers;
	}

	/**
	 * @param executable
	 *        The executable
	 * @param create
	 *        Whether to create a cache tag set if it doesn't exist
	 * @return The cache tags or null
	 */
	private static Set<String> getCacheTags( Executable executable, boolean create )
	{
		@SuppressWarnings("unchecked")
		Set<String> cacheTags = (Set<String>) executable.getAttributes().get( CACHE_TAGS_ATTRIBUTE );
		if( cacheTags == null && create )
		{
			cacheTags = new CopyOnWriteArraySet<String>();
			@SuppressWarnings("unchecked")
			Set<String> existing = (Set<String>) executable.getAttributes().putIfAbsent( CACHE_TAGS_ATTRIBUTE, cacheTags );
			if( existing != null )
				cacheTags = existing;
		}

		return cacheTags;
	}

	/**
	 * @return The cache expiration timestamp for the executable
	 */
	private static long getExpirationTimestamp( Executable executable )
	{
		Long cacheDurationLong = (Long) executable.getAttributes().get( CACHE_DURATION_ATTRIBUTE );
		long cacheDuration = cacheDurationLong == null ? 0 : cacheDurationLong;
		if( cacheDuration <= 0 )
			return 0;
		else
			return executable.getLastExecutedTimestamp() + cacheDuration;
	}

	/**
	 * Adds encoding to a cache key.
	 * 
	 * @param cacheKey
	 *        The cache key
	 * @param encoding
	 *        The encoding or null
	 * @return The cache key for the encoding
	 */
	private static String getCacheKeyForEncoding( String cacheKey, Encoding encoding )
	{
		if( encoding != null )
			return cacheKey + "|" + encoding.getName();
		else
			return cacheKey;
	}

	private void callCacheKeyPatternHandlers( Template template, DocumentDescriptor<Executable> documentDescriptor )
	{
		Map<String, String> resourceCacheKeyPatternHandlers = attributes.getCacheKeyPatternHandlers();
		Map<String, String> documentCacheKeyPatternHandlers = getCacheKeyPatternHandlers( documentDescriptor.getDocument(), false );

		// Make sure we have handlers
		if( ( ( resourceCacheKeyPatternHandlers == null ) || resourceCacheKeyPatternHandlers.isEmpty() ) && ( ( documentCacheKeyPatternHandlers == null ) || documentCacheKeyPatternHandlers.isEmpty() ) )
			return;

		// Merge all handlers
		Map<String, String> cacheKeyPatternHandlers = new HashMap<String, String>();
		if( resourceCacheKeyPatternHandlers != null )
			cacheKeyPatternHandlers.putAll( resourceCacheKeyPatternHandlers );
		if( documentCacheKeyPatternHandlers != null )
			cacheKeyPatternHandlers.putAll( documentCacheKeyPatternHandlers );

		// Group variables together per handler
		Map<String, Set<String>> delegatedHandlers = new HashMap<String, Set<String>>();
		List<String> variableNames = template.getVariableNames();
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

	/**
	 * Casts the cache key pattern for an executable.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 * @param encoding
	 *        The encoding or null
	 * @return The cache key or null
	 */
	private String castCacheKey( DocumentDescriptor<Executable> documentDescriptor )
	{
		String cacheKeyPattern = getCacheKeyPattern( documentDescriptor.getDocument() );
		if( cacheKeyPattern == null )
			return null;
		else
		{
			Request request = resource.getRequest();
			Response response = resource.getResponse();

			// Template and its resolver
			Template template = new Template( cacheKeyPattern );
			CacheKeyPatternResolver resolver = new CacheKeyPatternResolver( documentDescriptor, resource, conversationService, request, response );

			// Cache key pattern handlers
			callCacheKeyPatternHandlers( template, documentDescriptor );

			// Use captive reference as the resource reference
			Reference captiveReference = CapturingRedirector.getCapturedReference( request );
			Reference resourceReference = request.getResourceRef();
			if( captiveReference != null )
				request.setResourceRef( captiveReference );

			try
			{
				// Cast it
				return template.format( resolver );
			}
			finally
			{
				// Return regular reference
				if( captiveReference != null )
					request.setResourceRef( resourceReference );
			}
		}
	}

	/**
	 * Copies the cache tags for the current executable, if it has any, to the
	 * entire executable stack.
	 * 
	 * @param cacheTags
	 *        The cache tags
	 * @return The cleaned cache tags
	 */
	private Set<String> propagateCacheTags( Set<String> cacheTags )
	{
		ArrayList<String> propagatedCacheTags = new ArrayList<String>( cacheTags.size() );
		Set<String> cleanedCacheTags = new HashSet<String>( cacheTags.size() );

		for( String cacheTag : cacheTags )
		{
			// Don't propagate underscored cache tags
			if( cacheTag.startsWith( "_" ) )
				// But remove the underscore...
				cleanedCacheTags.add( cacheTag.substring( 1 ) );
			else
			{
				propagatedCacheTags.add( cacheTag );
				cleanedCacheTags.add( cacheTag );
			}
		}

		if( !propagatedCacheTags.isEmpty() )
		{
			DocumentDescriptor<Executable> currentDocumentDescriptor = popDocumentDescriptor();
			for( DocumentDescriptor<Executable> documentDescriptor : documentDescriptorStack )
				getCacheTags( documentDescriptor.getDocument(), true ).addAll( propagatedCacheTags );
			pushDocumentDescriptor( currentDocumentDescriptor );
		}

		return cleanedCacheTags;
	}

	/**
	 * Represents a cache entry, making sure to re-encode it (and store the
	 * re-encoded entry in the cache) if necessary.
	 * 
	 * @param cacheEntry
	 *        The cache entry
	 * @param encoding
	 *        The encoding
	 * @param cacheKey
	 *        The cache key
	 * @param executable
	 *        The executable
	 * @param writer
	 *        The writer
	 * @return The representation
	 * @throws IOException
	 */
	private Representation represent( CacheEntry cacheEntry, Encoding encoding, String cacheKey, Executable executable, Writer writer ) throws IOException
	{
		if( ( cacheEntry.getEncoding() == null ) && ( encoding != null ) )
		{
			// Re-encode it
			cacheEntry = new CacheEntry( cacheEntry, encoding );

			// Cache re-encoded entry
			Cache cache = attributes.getCache();
			if( cache != null )
			{
				String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, encoding );
				Set<String> cacheTags = getCacheTags( executable, false );
				if( cacheTags != null )
					cacheEntry.setTags( cacheTags.toArray( new String[] {} ) );
				cache.store( cacheKeyForEncoding, cacheEntry );
			}
		}

		// We want to write this, too, for includes
		if( ( writer != null ) && ( cacheEntry.getString() != null ) )
			writer.write( cacheEntry.getString() );

		return cacheEntry.represent();
	}

	/**
	 * Generates and possibly caches a textual representation. The returned
	 * representation is either a {@link StringRepresentation} or a
	 * {@link GeneratedTextDeferredRepresentation}. Text in the former case
	 * could be the result of either execution or retrieval from the cache.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 * @return A representation, either generated by the executable or fetched
	 *         from the cache
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	private Representation generateText( DocumentDescriptor<Executable> documentDescriptor ) throws IOException, ParsingException, ExecutionException
	{
		Executable executable = documentDescriptor.getDocument();
		Writer writer = executionContext.getWriter();

		// Optimized handling for pure text
		String pureText = executable.getAsPureLiteral();
		if( pureText != null )
		{
			// We want to write this, too, for includes
			if( writer != null )
				writer.write( pureText );

			return new CacheEntry( pureText, conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), getEncoding( executable ), executable.getDocumentTimestamp(),
				getExpirationTimestamp( executable ) ).represent();
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
				executionContext.setWriter( writer );
			}
			else
			{
				writer.flush();
				startPosition = writerBuffer.length();
			}

			// See if a valid cache entry has already been cached in the request
			CacheEntry cacheEntry = (CacheEntry) resource.getRequest().getAttributes().remove( CACHE_ENTRY_ATTRIBUTE );
			String cacheKey = (String) resource.getRequest().getAttributes().remove( CACHE_KEY_ATTRIBUTE );
			if( ( cacheEntry != null ) && ( cacheKey != null ) )
				return represent( cacheEntry, getEncoding( executable ), cacheKey, executable, writer );

			// Attempt to use cache
			cacheKey = castCacheKey( documentDescriptor );
			if( cacheKey != null )
			{
				Cache cache = attributes.getCache();
				if( cache != null )
				{
					// Try cache key for encoding first
					Encoding encoding = getEncoding( executable );
					String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, encoding );
					cacheEntry = cache.fetch( cacheKeyForEncoding );
					if( cacheEntry == null )
						cacheEntry = cache.fetch( cacheKey );

					// Make sure the document is not newer than the cache entry
					if( ( cacheEntry != null ) && ( executable.getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() ) )
						return represent( cacheEntry, encoding, cacheKey, executable, writer );
				}
			}
		}

		setCacheDuration( 0 );
		setCacheKeyPattern( attributes.getDefaultCacheKeyPattern() );
		getCacheTags().clear();

		try
		{
			executionContext.setWriter( writer );
			executionContext.getServices().put( attributes.getDocumentServiceName(), this );
			executionContext.getServices().put( attributes.getApplicationServiceName(), applicationService );
			executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

			// Execute!
			executable.execute( executionContext, this, attributes.getExecutionController() );

			// Propagate cache tags up the stack
			Set<String> cacheTags = getCacheTags( executable, false );
			if( ( cacheTags != null ) && !cacheTags.isEmpty() )
				cacheTags = propagateCacheTags( cacheTags );

			// Did the executable ask us to defer?
			if( conversationService.defer )
			{
				conversationService.defer = false;

				// Note that this will cause the executable to execute
				// again!
				GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this );
				return new GeneratedTextDeferredRepresentation( documentService.resource, executable, documentService.executionContext, documentService, documentService.conversationService );
			}

			if( conversationService.isDeferred )
			{
				// Nothing to return in deferred mode
				return null;
			}
			else
			{
				writer.flush();

				Encoding encoding = getEncoding( executable );
				long expirationTimestamp = getExpirationTimestamp( executable );

				// Get the buffer from when we executed the executable
				CacheEntry cacheEntry = new CacheEntry( writerBuffer.substring( startPosition ), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), null,
					executable.getDocumentTimestamp(), expirationTimestamp );

				// Encoded version
				CacheEntry encodedCacheEntry = new CacheEntry( cacheEntry, encoding );

				// Cache if enabled
				if( expirationTimestamp > 0 )
				{
					String cacheKey = castCacheKey( documentDescriptor );
					if( cacheKey != null )
					{
						// Cache!
						Cache cache = attributes.getCache();
						if( cache != null )
						{
							String[] tags = null;
							if( cacheTags != null )
								tags = cacheTags.toArray( new String[] {} );

							String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, encoding );
							encodedCacheEntry.setTags( tags );
							cache.store( cacheKeyForEncoding, encodedCacheEntry );

							// Cache un-encoded entry separately
							if( encoding != null )
							{
								cacheEntry.setTags( tags );
								cache.store( cacheKey, cacheEntry );
							}
						}
					}
				}

				// Return a representation of the entire buffer
				if( startPosition == 0 )
					return encodedCacheEntry.represent();
				else
					return new CacheEntry( encodedCacheEntry, writerBuffer.toString() ).represent();
			}
		}
		catch( ExecutionException x )
		{
			// Did the executable ask us to defer?
			if( conversationService.defer )
			{
				// Note that we will allow exceptions in an executable that
				// ask us to defer! In fact, throwing an exception is a good
				// way for the executable to signal that it's done and is
				// ready to defer.

				conversationService.defer = false;

				// Note that this will cause the executable to run again!
				GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this );
				return new GeneratedTextDeferredRepresentation( documentService.resource, executable, documentService.executionContext, documentService, documentService.conversationService );
			}
			else
				throw x;
		}
		finally
		{
			writer.flush();
			executionContext.getErrorWriterOrDefault().flush();
		}
	}
}