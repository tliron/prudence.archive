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

package com.threecrickets.prudence.internal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.routing.Template;
import org.restlet.routing.Variable;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * This is the <code>prudence</code> variable exposed to scriptlets.
 * 
 * @author Tal Liron
 */
public class ExposedContainerForGeneratedTextResource extends ExposedContainerBase<GeneratedTextResource>
{
	//
	// Construction
	//

	/**
	 * Constructs a container with media type and character set according to the
	 * variant, or {@link GeneratedTextResource#getDefaultCharacterSet()} if
	 * none is provided.
	 * 
	 * @param resource
	 *        The resource
	 * @param executionContext
	 *        The execution context
	 * @param exposedConversation
	 *        The exposed conversation
	 */
	public ExposedContainerForGeneratedTextResource( GeneratedTextResource resource, ExecutionContext executionContext, Representation entity, Variant variant )
	{
		super( resource, resource.getDocumentSource() );
		this.executionContext = executionContext;
		this.exposedConversation = new ExposedConversationForGeneratedTextResource( resource, entity, variant, resource.getDefaultCharacterSet() );
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
		Long cacheDuration = (Long) executable.getAttributes().get( CACHE_DURATION_ATTRIBUTE );
		return cacheDuration == null ? 0 : cacheDuration;
	}

	/**
	 * @param cacheDuration
	 *        The cache duration in milliseconds
	 * @see #getCacheDuration()
	 */
	public void setCacheDuration( long cacheDuration )
	{
		executable.getAttributes().put( CACHE_DURATION_ATTRIBUTE, cacheDuration );
	}

	/**
	 * @return The cache key pattern
	 * @see #setCacheKey(String)
	 */
	public String getCacheKey()
	{
		return (String) executable.getAttributes().get( CACHE_KEY_ATTRIBUTE );
	}

	/**
	 * @param cacheKey
	 *        The cache key pattern
	 * @see #getCacheKey()
	 */
	public void setCacheKey( String cacheKey )
	{
		executable.getAttributes().put( CACHE_KEY_ATTRIBUTE, cacheKey );
	}

	/**
	 * @return The cache group keys
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getCacheGroups()
	{
		Set<String> cacheGroups = (Set<String>) executable.getAttributes().get( CACHE_GROUPS_ATTRIBUTE );
		if( cacheGroups == null )
		{
			cacheGroups = new HashSet<String>();
			Set<String> existing = (Set<String>) executable.getAttributes().putIfAbsent( CACHE_GROUPS_ATTRIBUTE, cacheGroups );
			if( existing != null )
				cacheGroups = existing;

		}
		return cacheGroups;
	}

	//
	// Operations
	//

	/**
	 * This powerful method allows scriptlets to execute other documents in
	 * place, and is useful for creating large, maintainable applications based
	 * on documents. Included documents can act as a library or toolkit and can
	 * even be shared among many applications. The included document does not
	 * have to be in the same programming language or use the same engine as the
	 * calling scriptlet. However, if they do use the same engine, then methods,
	 * functions, modules, etc., could be shared.
	 * <p>
	 * It is important to note that how this works varies a lot per engine. For
	 * example, in JRuby, every scriptlet is run in its own scope, so that
	 * sharing would have to be done explicitly in the global scope. See the
	 * included JRuby examples for a discussion of various ways to do this.
	 * 
	 * @param documentName
	 *        The document name
	 * @return A representation of the executable's output
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	public Representation includeDocument( String documentName ) throws IOException, ParsingException, ExecutionException
	{
		DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, resource.getDocumentSource(), true, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		executable = documentDescriptor.getDocument();

		if( exposedConversation.getMediaType() == null )
		{
			// Set initial media type according to the document's tag
			exposedConversation.setMediaTypeExtension( documentDescriptor.getTag() );
		}

		return execute( executable );
	}

	/**
	 * As {@link #includeDocument(String)}, except that the document is parsed
	 * as a single, non-delimited script with the engine name derived from
	 * name's extension.
	 * 
	 * @param documentName
	 *        The document name
	 * @return A representation of the executable's output
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	@Override
	public Representation include( String documentName ) throws IOException, ParsingException, ExecutionException
	{
		DocumentDescriptor<Executable> documentDescriptor = Executable
			.createOnce( documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		executable = documentDescriptor.getDocument();

		return execute( executable );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String NAME_VARIABLE = "n";

	private static final String CACHE_DURATION_ATTRIBUTE = "prudence.cacheDuration";

	private static final String CACHE_KEY_ATTRIBUTE = "prudence.cacheKey";

	private static final String CACHE_GROUPS_ATTRIBUTE = "prudence.cacheGroups";

	/**
	 * The exposed conversation.
	 */
	private final ExposedConversationForGeneratedTextResource exposedConversation;

	/**
	 * This boolean is true when the writer is in streaming mode.
	 */
	protected boolean isStreaming;

	/**
	 * Buffer used for caching mode.
	 */
	private StringBuffer buffer;

	/**
	 * The execution context.
	 */
	private final ExecutionContext executionContext;

	/**
	 * The currently executing executable.
	 */
	private Executable executable;

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
			template.getVariables().put( NAME_VARIABLE, new Variable( Variable.TYPE_ALL, executable.getDocumentName(), true, true ) );
			return template.format( resource.getRequest(), resource.getResponse() );
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
			return executable.getLastExecutedTimestamp() + cacheDuration;
	}

	/**
	 * The actual execution of an executable.
	 * 
	 * @param executable
	 *        The executable
	 * @return A representation, either generated by the executable or fetched
	 *         from the cache
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	private Representation execute( Executable executable ) throws IOException, ParsingException, ExecutionException
	{
		this.executable = executable;

		Writer writer = resource.getWriter();

		// Optimized handling for pure text
		String pureText = executable.getAsPureText();
		if( pureText != null )
		{
			// We want to write this, too, for includes
			if( writer != null )
				writer.write( pureText );

			return new CacheEntry( pureText, exposedConversation.getMediaType(), exposedConversation.getLanguage(), exposedConversation.getCharacterSet(), getExpiration() ).represent();
		}

		int startPosition = 0;

		// Make sure we have a valid writer for caching mode
		if( !exposedConversation.isStreaming )
		{
			if( writer == null )
			{
				StringWriter stringWriter = new StringWriter();
				buffer = stringWriter.getBuffer();
				writer = new BufferedWriter( stringWriter );

				// Make sure that included executables use the same writer
				resource.setWriter( writer );
			}
			else
			{
				writer.flush();
				startPosition = buffer.length();
			}

			// Attempt to use cache
			String cacheKey = castCacheKeyPattern();
			if( cacheKey != null )
			{
				CacheEntry cacheEntry = resource.getCache().fetch( cacheKey );
				if( cacheEntry != null )
				{
					if( writer != null )
						writer.write( cacheEntry.getString() );

					return cacheEntry.represent();
				}
			}
		}

		setCacheDuration( 0 );
		setCacheKey( resource.getDefaultCacheKey() );
		getCacheGroups().clear();

		try
		{
			executionContext.setWriter( writer );
			executionContext.getExposedVariables().put( resource.getContainerName(), this );
			executionContext.getExposedVariables().put( resource.getConversationName(), exposedConversation );

			// Execute!
			executable.execute( executionContext, this, resource.getExecutionController() );

			// Executable might have changed
			this.executable = executable;

			// Did the executable ask us to start streaming?
			if( exposedConversation.startStreaming )
			{
				exposedConversation.startStreaming = false;

				// Note that this will cause the executable to execute again!
				// TODO: flushLines!
				return new GeneratedTextStreamingRepresentation( this, exposedConversation, executionContext, resource.getExecutionController(), executable );
			}

			if( isStreaming )
			{
				// Nothing to return in streaming mode
				return null;
			}
			else
			{
				writer.flush();

				// Get the buffer from when we executed the executable
				CacheEntry cacheEntry = new CacheEntry( buffer.substring( startPosition ), exposedConversation.getMediaType(), exposedConversation.getLanguage(), exposedConversation.getCharacterSet(), getExpiration() );

				// Cache if enabled
				String cacheKey = castCacheKeyPattern();
				Collection<String> cacheGroups = getCacheGroups();
				if( ( cacheKey != null ) && ( cacheEntry.getExpirationDate() != null ) )
					resource.getCache().store( cacheKey, cacheGroups, cacheEntry );

				// Return a representation of the entire buffer
				if( startPosition == 0 )
					return cacheEntry.represent();
				else
					return new CacheEntry( buffer.toString(), exposedConversation.getMediaType(), exposedConversation.getLanguage(), exposedConversation.getCharacterSet(), getExpiration() ).represent();
			}
		}
		catch( ExecutionException x )
		{
			// Did the executable ask us to start streaming?
			if( exposedConversation.startStreaming )
			{
				exposedConversation.startStreaming = false;

				// Note that this will cause the executable to run again!
				// TODO: flushLines!
				return new GeneratedTextStreamingRepresentation( this, exposedConversation, executionContext, resource.getExecutionController(), executable );

				// Note that we will allow exceptions in executable that ask us
				// to start streaming! In fact, throwing an exception is a
				// good way for the executable to signal that it's done and is
				// ready to start streaming.
			}
			else
				throw x;
		}
	}
}