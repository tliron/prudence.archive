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
import java.util.ArrayList;
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
public class GeneratedTextResourceDocumentService extends ResourceDocumentServiceBase<GeneratedTextResource>
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
		conversationService = new GeneratedTextResourceConversationService( resource, entity, variant, resource.getDefaultCharacterSet() );
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
		super( documentService.resource, documentService.resource.getDocumentSource() );
		conversationService = documentService.conversationService;
		pushDocumentDescriptor( documentService.getCurrentDocumentDescriptor() );
		executionContext = new ExecutionContext();

		// Initialize execution context
		executionContext.getServices().put( resource.getDocumentServiceName(), this );
		executionContext.getServices().put( resource.getApplicationServiceName(), applicationService );
		executionContext.getServices().put( resource.getConversationServiceName(), conversationService );
		File libraryDirectory = resource.getLibraryDirectory();
		if( libraryDirectory != null )
			executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

		conversationService.isDeferred = true;
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
	 * @return The cache key pattern
	 * @see #setCacheKey(String)
	 */
	public String getCacheKey()
	{
		return (String) getCurrentDocumentDescriptor().getDocument().getAttributes().get( CACHE_KEY_ATTRIBUTE );
	}

	/**
	 * @param cacheKey
	 *        The cache key pattern
	 * @see #getCacheKey()
	 */
	public void setCacheKey( String cacheKey )
	{
		getCurrentDocumentDescriptor().getDocument().getAttributes().put( CACHE_KEY_ATTRIBUTE, cacheKey );
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
		return include( documentName, true );
	}

	/**
	 * Includes a text document into the current location. The document may be a
	 * "text-with-scriptlets" executable, in which case its output could be
	 * dynamically generated.
	 * 
	 * @param documentName
	 *        The document name
	 * @param allowFragments
	 *        Whether to allow documents in the fragments directory
	 * @return A representation of the document's output
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 * @see GeneratedTextResource#getFragmentDirectory()
	 */
	public Representation include( String documentName, boolean allowFragments ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = resource.validateDocumentName( documentName );

		DocumentDescriptor<Executable> documentDescriptor;
		try
		{
			documentDescriptor = Executable.createOnce( documentName, resource.getDocumentSource(), true, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		}
		catch( DocumentNotFoundException x )
		{
			if( allowFragments )
			{
				// Try the fragment directory
				File fragmentDirectory = resource.getFragmentDirectoryRelative();
				if( fragmentDirectory != null )
					documentDescriptor = Executable.createOnce( fragmentDirectory.getPath() + "/" + documentName, resource.getDocumentSource(), true, resource.getLanguageManager(), resource.getDefaultLanguageTag(),
						resource.isPrepare() );
				else
					throw x;
			}
			else
				throw x;
		}

		// Add dependency
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		if( currentDocumentDescriptor != null )
			currentDocumentDescriptor.getDependencies().add( documentDescriptor.getDefaultName() );

		if( conversationService.getMediaType() == null )
			// Set initial media type according to the document's tag
			conversationService.setMediaTypeExtension( documentDescriptor.getTag() );

		// Execute
		pushDocumentDescriptor( documentDescriptor );
		try
		{
			return execute( documentDescriptor.getDocument() );
		}
		finally
		{
			popDocumentDescriptor();
		}
	}

	//
	// ResourceDocumentService
	//

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

		DocumentDescriptor<Executable> documentDescriptor;
		try
		{
			documentDescriptor = Executable.createOnce( documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		}
		catch( DocumentNotFoundException x )
		{
			File libraryDirectory = resource.getLibraryDirectoryRelative();
			if( libraryDirectory != null )
				// Try the library directory
				documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(),
					resource.isPrepare() );
			else
				throw x;
		}

		// Add dependency
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		if( currentDocumentDescriptor != null )
			currentDocumentDescriptor.getDependencies().add( documentDescriptor.getDefaultName() );

		// Execute
		pushDocumentDescriptor( documentDescriptor );
		try
		{
			return execute( documentDescriptor.getDocument() );
		}
		finally
		{
			popDocumentDescriptor();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String DOCUMENT_NAME_VARIABLE = "dn";

	private static final String DOCUMENT_NAME_VARIABLE_FULL = "{" + DOCUMENT_NAME_VARIABLE + "}";

	private static final String APPLICATION_NAME_VARIABLE = "an";

	private static final String APPLICATION_NAME_VARIABLE_FULL = "{" + APPLICATION_NAME_VARIABLE + "}";

	private static final String PATH_TO_BASE_VARIABLE = "ptb";

	private static final String PATH_TO_BASE_VARIABLE_FULL = "{" + PATH_TO_BASE_VARIABLE + "}";

	private static final String CACHE_DURATION_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheDuration";

	private static final String CACHE_KEY_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheKey";

	private static final String CACHE_TAGS_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cacheTags";

	private static final String CACHED_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.cached";

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

			Reference captiveReference = CaptiveRedirector.getCaptiveReference( resource.getRequest() );
			Reference resourceReference = resource.getRequest().getResourceRef();

			// Our additional template variables: {dn}, {an} and {ptb}

			if( cacheKey.contains( DOCUMENT_NAME_VARIABLE_FULL ) )
				template.getVariables().put( DOCUMENT_NAME_VARIABLE, new Variable( Variable.TYPE_ALL, getCurrentDocumentDescriptor().getDefaultName(), true, true ) );

			if( cacheKey.contains( APPLICATION_NAME_VARIABLE_FULL ) )
				template.getVariables().put( APPLICATION_NAME_VARIABLE, new Variable( Variable.TYPE_ALL, resource.getApplication().getName(), true, true ) );

			if( cacheKey.contains( PATH_TO_BASE_VARIABLE_FULL ) )
			{
				Reference reference = captiveReference != null ? captiveReference : resourceReference;
				String pathToBase = reference.getBaseRef().getRelativeRef( reference ).getPath();
				template.getVariables().put( PATH_TO_BASE_VARIABLE, new Variable( Variable.TYPE_ALL, pathToBase, true, true ) );
			}

			// Use captive reference as the resource reference
			if( captiveReference != null )
				resource.getRequest().setResourceRef( captiveReference );

			String cast = template.format( resource.getRequest(), resource.getResponse() );

			// Return regular reference
			if( captiveReference != null )
				resource.getRequest().setResourceRef( resourceReference );

			return cast;
		}
	}

	/**
	 * @param executable
	 *        The executable
	 * @param create
	 *        Whether to create a cache tag set if it doesn't exist
	 * @return The cache tags or null
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getCacheTags( Executable executable, boolean create )
	{
		Set<String> cacheTags = (Set<String>) executable.getAttributes().get( CACHE_TAGS_ATTRIBUTE );
		if( cacheTags == null && create )
		{
			cacheTags = new HashSet<String>();
			Set<String> existing = (Set<String>) executable.getAttributes().putIfAbsent( CACHE_TAGS_ATTRIBUTE, cacheTags );
			if( existing != null )
				cacheTags = existing;

		}
		return cacheTags;
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
	 * @return The cache expiration timestamp for the executable
	 */
	private long getExpiration()
	{
		long cacheDuration = getCacheDuration();
		if( cacheDuration <= 0 )
			return 0;
		else
			return getCurrentDocumentDescriptor().getDocument().getLastExecutedTimestamp() + cacheDuration;
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
		Writer writer = executionContext.getWriter();

		// Optimized handling for pure text
		String pureText = executable.getAsPureLiteral();
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
				executionContext.setWriter( writer );
			}
			else
			{
				writer.flush();
				startPosition = writerBuffer.length();
			}

			// Attempt to use cache, if executable is branded as cached
			if( executable.getAttributes().containsKey( CACHED_ATTRIBUTE ) )
			{
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
		}

		setCacheDuration( 0 );
		setCacheKey( resource.getDefaultCacheKey() );
		getCacheTags().clear();

		try
		{
			executionContext.setWriter( writer );
			executionContext.getServices().put( resource.getDocumentServiceName(), this );
			executionContext.getServices().put( resource.getApplicationServiceName(), applicationService );
			executionContext.getServices().put( resource.getConversationServiceName(), conversationService );

			// Execute!
			executable.execute( executionContext, this, resource.getExecutionController() );

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

				// Get the buffer from when we executed the executable
				CacheEntry cacheEntry = new CacheEntry( writerBuffer.substring( startPosition ), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(),
					getExpiration() );

				// Cache if enabled
				String cacheKey = castCacheKeyPattern();
				if( ( cacheKey != null ) && ( cacheEntry.getExpirationDate() != null ) )
				{
					Set<String> cacheTags = getCacheTags( executable, false );

					// Propagate cache tags up the stack
					if( ( cacheTags != null ) && !cacheTags.isEmpty() )
						cacheTags = propagateCacheTags( cacheTags );

					Cache cache = resource.getCache();
					if( cache != null )
					{
						// Cache!
						cache.store( cacheKey, cacheTags, cacheEntry );

						// We're branding the executable as cached; if the
						// executable is regenerated for some reason, it would
						// no longer have this brand
						executable.getAttributes().put( CACHED_ATTRIBUTE, true );
					}
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