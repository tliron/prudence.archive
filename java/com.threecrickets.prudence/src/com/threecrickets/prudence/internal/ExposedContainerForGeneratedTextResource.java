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

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.routing.Template;
import org.restlet.routing.Variable;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.LanguageAdapter;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.ExecutableInitializationException;
import com.threecrickets.scripturian.exception.ExecutionException;

/**
 * This is the <code>prudence</code> variable exposed to scriptlets.
 * 
 * @author Tal Liron
 */
public class ExposedContainerForGeneratedTextResource extends ExposedContainerBase
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
	 * @param entity
	 *        The entity
	 * @param variant
	 *        The variant
	 * @param cache
	 *        The cache (used for caching mode)
	 */
	public ExposedContainerForGeneratedTextResource( GeneratedTextResource resource, Representation entity, Variant variant, Cache cache )
	{
		this.resource = resource;
		this.entity = entity;
		this.variant = variant;
		this.cache = cache;

		if( variant != null )
		{
			mediaType = variant.getMediaType();
			characterSet = variant.getCharacterSet();
		}

		if( characterSet == null )
			characterSet = resource.getDefaultCharacterSet();

		executionContext = new ExecutionContext( resource.getLanguageManager() );
	}

	//
	// Attributes
	//

	/**
	 * The {@link CharacterSet} that will be used for the generated string.
	 * Defaults to what the client requested (in container.variant), or to the
	 * value of {@link GeneratedTextResource#defaultCharacterSet} if the client
	 * did not specify it. If not in streaming mode, your script can change this
	 * to something else.
	 * 
	 * @return The character set
	 * @see #setCharacterSet(CharacterSet)
	 */
	public CharacterSet getCharacterSet()
	{
		return characterSet;
	}

	/**
	 * Throws an {@link IllegalStateException} if in streaming mode.
	 * 
	 * @param characterSet
	 *        The character set
	 * @see #getCharacterSet()
	 */
	public void setCharacterSet( CharacterSet characterSet )
	{
		if( isStreaming() )
			throw new IllegalStateException( "Cannot change character set while streaming" );

		this.characterSet = characterSet;
	}

	/**
	 * @return The character set name
	 * @see #getCharacterSet()
	 */
	public String getCharacterSetName()
	{
		return characterSet != null ? characterSet.getName() : null;
	}

	/**
	 * @param characterSetName
	 *        The character set name
	 * @see #setCharacterSet(CharacterSet)
	 */
	public void setCharacterSetName( String characterSetName )
	{
		characterSet = CharacterSet.valueOf( characterSetName );
	}

	/**
	 * @return The character set extension
	 * @see #getCharacterSet()
	 */
	public String getCharacterSetExtension()
	{
		return characterSet != null ? resource.getApplication().getMetadataService().getExtension( characterSet ) : null;
	}

	/**
	 * @param characterSetExtension
	 *        The character set extension
	 * @see #setCharacterSet(CharacterSet)
	 */
	public void setCharacterSetExtension( String characterSetExtension )
	{
		characterSet = resource.getApplication().getMetadataService().getCharacterSet( characterSetExtension );
	}

	/**
	 * The {@link Language} that will be used for the generated string. Defaults
	 * to null. If not in streaming mode, your script can change this to
	 * something else.
	 * 
	 * @return The language or null if set
	 * @see #setLanguage(Language)
	 */
	public Language getLanguage()
	{
		return language;
	}

	/**
	 * Throws an {@link IllegalStateException} if in streaming mode.
	 * 
	 * @param language
	 *        The language or null
	 * @see #getLanguage()
	 */
	public void setLanguage( Language language )
	{
		if( isStreaming() )
			throw new IllegalStateException( "Cannot change language while streaming" );

		this.language = language;
	}

	/**
	 * @return The language name
	 * @see #getLanguage()
	 */
	public String getLanguageName()
	{
		return language != null ? language.getName() : null;
	}

	/**
	 * @param languageName
	 *        The language name
	 * @see #setLanguage(Language)
	 */
	public void setLanguageName( String languageName )
	{
		language = Language.valueOf( languageName );
	}

	/**
	 * @return The language extension
	 * @see #getLanguage()
	 */
	public String getLanguageExtension()
	{
		return language != null ? resource.getApplication().getMetadataService().getExtension( language ) : null;
	}

	/**
	 * @param languageExtension
	 *        The language extension
	 * @see #setLanguage(Language)
	 */
	public void setLanguageExtension( String languageExtension )
	{
		language = resource.getApplication().getMetadataService().getLanguage( languageExtension );
	}

	/**
	 * The {@link MediaType} that will be used for the generated string.
	 * Defaults to what the client requested (in container.variant). If not in
	 * streaming mode, your script can change this to something else.
	 * 
	 * @return The media type
	 * @see #setMediaType(MediaType)
	 */
	public MediaType getMediaType()
	{
		return mediaType;
	}

	/**
	 * Throws an {@link IllegalStateException} if in streaming mode.
	 * 
	 * @param mediaType
	 *        The media type
	 * @see #getMediaType()
	 */
	public void setMediaType( MediaType mediaType )
	{
		if( isStreaming() )
			throw new IllegalStateException( "Cannot change media type while streaming" );

		this.mediaType = mediaType;
	}

	/**
	 * @return The media type name
	 * @see #getMediaType()
	 */
	public String getMediaTypeName()
	{
		return mediaType != null ? mediaType.getName() : null;
	}

	/**
	 * @param mediaTypeName
	 *        The media type name
	 * @see #setMediaType(MediaType)
	 */
	public void setMediaTypeName( String mediaTypeName )
	{
		mediaType = MediaType.valueOf( mediaTypeName );
	}

	/**
	 * @return The media type extension
	 * @see #getMediaType()
	 */
	public String getMediaTypeExtension()
	{
		return mediaType != null ? resource.getApplication().getMetadataService().getExtension( mediaType ) : null;
	}

	/**
	 * @param mediaTypeExtension
	 *        The media type extension
	 * @see #setMediaType(MediaType)
	 */
	public void setMediaTypeExtension( String mediaTypeExtension )
	{
		mediaType = resource.getApplication().getMetadataService().getMediaType( mediaTypeExtension );
	}

	/**
	 * The response status code.
	 * 
	 * @return The response status code
	 * @see #setStatusCode(int)
	 */
	public int getStatusCode()
	{
		return resource.getResponse().getStatus().getCode();
	}

	/**
	 * The response status code.
	 * 
	 * @param statusCode
	 *        The response status code
	 * @see #getStatusCode()
	 */
	public void setStatusCode( int statusCode )
	{
		resource.getResponse().setStatus( Status.valueOf( statusCode ) );
	}

	/**
	 * The instance of this resource. Acts as a "this" reference for scriptlets.
	 * You can use it to access the request and response.
	 * 
	 * @return The resource
	 */
	public GeneratedTextResource getResource()
	{
		return resource;
	}

	/**
	 * The entity of this request. Available only for post and put.
	 * 
	 * @return The entity
	 */
	public Representation getEntity()
	{
		return entity;
	}

	/**
	 * The {@link Variant} of this request. Useful for interrogating the
	 * client's preferences.
	 * 
	 * @return The variant
	 */
	public Variant getVariant()
	{
		return variant;
	}

	/**
	 * The {@link DocumentSource} used to fetch executables.
	 * 
	 * @return The document source
	 */
	public DocumentSource<Executable> getSource()
	{
		return resource.getDocumentSource();
	}

	/**
	 * This boolean is true when the writer is in streaming mode.
	 * 
	 * @return True if in streaming mode, false if in caching mode
	 */
	public boolean isStreaming()
	{
		return isStreaming;
	}

	/**
	 * Identical to {@link #isStreaming()}. Supports scripting engines which
	 * don't know how to recognize the "is" getter notation, but can recognize
	 * the "get" notation.
	 * 
	 * @return True if in streaming mode, false if in caching mode
	 * @see #isStreaming()
	 */
	public boolean getIsStreaming()
	{
		return isStreaming();
	}

	/**
	 * Checks if the request was received via the RIAP protocol
	 * 
	 * @return True if the request was received via the RIAP protocol
	 * @see LocalReference
	 */
	public boolean isInternal()
	{
		return resource.getRequest().getResourceRef().getSchemeProtocol().equals( Protocol.RIAP );
	}

	/**
	 * Identical to {@link #isInternal()}. Supports scripting engines which
	 * don't know how to recognize the "is" getter notation, but can recognize
	 * the "get" notation.
	 * 
	 * @return True if the request was received via the RIAP protocol
	 * @see #isInternal()
	 */
	public boolean getIsInternal()
	{
		return isInternal();
	}

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
	 * @param name
	 *        The script name
	 * @return A representation of the script's output
	 * @throws IOException
	 * @throws ExecutableInitializationException
	 * @throws ExecutionException
	 */
	public Representation includeDocument( String name ) throws IOException, ExecutableInitializationException, ExecutionException
	{
		DocumentDescriptor<Executable> documentDescriptor = resource.getDocumentSource().getDocument( name );

		Executable executable = documentDescriptor.getDocument();
		if( executable == null )
		{
			String sourceCode = documentDescriptor.getSourceCode();
			executable = new Executable( documentDescriptor.getDefaultName(), sourceCode, true, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.getDocumentSource(), resource
				.isAllowCompilation() );

			Executable existing = documentDescriptor.setDocumentIfAbsent( executable );
			if( existing != null )
				executable = existing;
		}

		if( getMediaType() == null )
		{
			// Set initial media type according to the document's tag
			setMediaType( resource.getMetadataService().getMediaType( documentDescriptor.getTag() ) );
		}

		return execute( executable );
	}

	/**
	 * As {@link #includeDocument(String)}, except that the document is parsed
	 * as a single, non-delimited script with the engine name derived from
	 * name's extension.
	 * 
	 * @param name
	 *        The script name
	 * @return A representation of the script's output
	 * @throws IOException
	 * @throws ExecutableInitializationException
	 * @throws ExecutionException
	 */
	public Representation include( String name ) throws IOException, ExecutableInitializationException, ExecutionException
	{
		DocumentDescriptor<Executable> documentDescriptor = resource.getDocumentSource().getDocument( name );

		Executable executable = documentDescriptor.getDocument();
		if( executable == null )
		{
			LanguageAdapter languageAdapter = resource.getLanguageManager().getAdapterByExtension( name, documentDescriptor.getTag() );
			String sourceCode = documentDescriptor.getSourceCode();
			executable = new Executable( documentDescriptor.getDefaultName(), sourceCode, false, resource.getLanguageManager(), (String) languageAdapter.getAttributes().get( LanguageAdapter.DEFAULT_TAG ), resource
				.getDocumentSource(), resource.isAllowCompilation() );

			Executable existing = documentDescriptor.setDocumentIfAbsent( executable );
			if( existing != null )
				executable = existing;
		}

		return execute( executable );
	}

	/**
	 * If you are in caching mode, calling this method will return true and
	 * cause the document to run again, where this next run will be in streaming
	 * mode. Whatever output the document created in the current run is
	 * discarded, and all further exceptions are ignored. For this reason, it's
	 * probably best to call <code>prudence.stream()</code> as early as possible
	 * in the document, and then to quit the document as soon as possible if it
	 * returns true. For example, your document can start by testing whether it
	 * will have a lot of output, and if so, set output characteristics, call
	 * <code>prudence.stream()</code>, and quit. If you are already in streaming
	 * mode, calling this method has no effect and returns false. Note that a
	 * good way to quit the script is to throw an exception, because it will end
	 * the script and otherwise be ignored.
	 * <p>
	 * By default, writers will be automatically flushed after every line in
	 * streaming mode. If you want to disable this behavior, use
	 * {@link #stream(boolean)}.
	 * 
	 * @return True if started streaming mode, false if already in streaming
	 *         mode
	 * @see #stream(boolean)
	 */
	public boolean stream()
	{
		return stream( true );
	}

	/**
	 * This version of {@link #stream()} adds a boolean argument to let you
	 * control whether to flush the writer after every line in streaming mode.
	 * By default auto-flushing is enabled.
	 * 
	 * @param flushLines
	 *        Whether to flush the writers after every line in streaming mode
	 * @return True if started streaming mode, false if already in streaming
	 *         mode
	 * @see #stream()
	 */
	public boolean stream( boolean flushLines )
	{
		if( isStreaming() )
			return false;

		startStreaming = true;
		this.flushLines = flushLines;
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String CACHE_DURATION_ATTRIBUTE = "prudence.cacheDuration";

	private static final String CACHE_KEY_ATTRIBUTE = "prudence.cacheKey";

	private static final String CACHE_GROUPS_ATTRIBUTE = "prudence.cacheGroups";

	private static final String NAME_VARIABLE = "n";

	/**
	 * The resource.
	 */
	private final GeneratedTextResource resource;

	/**
	 * Flag to signify that we should enter streaming mode.
	 */
	private boolean startStreaming;

	/**
	 * The entity of this request.
	 */
	private final Representation entity;

	/**
	 * The {@link Variant} of this request.
	 */
	private final Variant variant;

	/**
	 * Cache for caching mode.
	 */
	private final Cache cache;

	/**
	 * Whether to flush the writers after every line in streaming mode.
	 */
	private boolean flushLines;

	/**
	 * The {@link CharacterSet} that will be used for the generated string.
	 */
	private CharacterSet characterSet;

	/**
	 * The {@link Language} that will be used for the generated string.
	 */
	private Language language;

	/**
	 * The {@link MediaType} that will be used for the generated string.
	 */
	private MediaType mediaType;

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
	private String castCacheKey()
	{
		String cacheKey = getCacheKey();
		if( cacheKey == null )
			return null;
		else
		{
			Template template = new Template( cacheKey );
			template.getVariables().put( NAME_VARIABLE, new Variable( Variable.TYPE_ALL, executable.getName(), true, true ) );
			return template.format( getResource().getRequest(), getResource().getResponse() );
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
	 * @throws ExecutableInitializationException
	 * @throws ExecutionException
	 */
	private Representation execute( Executable executable ) throws IOException, ExecutableInitializationException, ExecutionException
	{
		this.executable = executable;

		boolean isStreaming = isStreaming();
		Writer writer = resource.getWriter();

		// Special handling for pure text
		String pureText = executable.getAsPureText();
		if( pureText != null )
		{
			if( writer != null )
				writer.write( pureText );

			return new CacheEntry( pureText, getMediaType(), getLanguage(), getCharacterSet(), getExpiration() ).represent();
		}

		int startPosition = 0;

		// Make sure we have a valid writer for caching mode
		if( !isStreaming )
		{
			if( writer == null )
			{
				StringWriter stringWriter = new StringWriter();
				buffer = stringWriter.getBuffer();
				writer = new BufferedWriter( stringWriter );
				resource.setWriter( writer );
			}
			else
			{
				writer.flush();
				startPosition = buffer.length();
			}

			// Attempt to use cache
			String cacheKey = castCacheKey();
			if( cacheKey != null )
			{
				CacheEntry cacheEntry = cache.fetch( cacheKey );
				if( cacheEntry != null )
				{
					if( writer != null )
						writer.write( cacheEntry.getString() );

					return cacheEntry.represent();
				}
			}
		}

		PrudenceExecutionController<ExposedContainerForGeneratedTextResource> executionController = new PrudenceExecutionController<ExposedContainerForGeneratedTextResource>( this, resource.getContainerName(), resource
			.getExecutionController() );

		setCacheDuration( 0 );
		setCacheKey( resource.getDefaultCacheKey() );
		getCacheGroups().clear();

		try
		{
			executable.execute( false, writer, resource.getErrorWriter(), false, executionContext, this, executionController );

			// Executable might have changed!
			this.executable = executable;

			// Did the executable ask us to start streaming?
			if( startStreaming )
			{
				startStreaming = false;

				// Note that this will cause the executable to execute again!
				return new GeneratedTextStreamingRepresentation( resource, this, executionContext, resource.getExecutionController(), executable, flushLines );
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
				CacheEntry cacheEntry = new CacheEntry( buffer.substring( startPosition ), getMediaType(), getLanguage(), getCharacterSet(), getExpiration() );

				// Cache if enabled
				String cacheKey = castCacheKey();
				Collection<String> cacheGroups = getCacheGroups();
				if( ( cacheKey != null ) && ( cacheEntry.getExpirationDate() != null ) )
					cache.store( cacheKey, cacheGroups, cacheEntry );

				// Return a representation of the entire buffer
				if( startPosition == 0 )
					return cacheEntry.represent();
				else
					return new CacheEntry( buffer.toString(), getMediaType(), getLanguage(), getCharacterSet(), getExpiration() ).represent();
			}
		}
		catch( ExecutionException x )
		{
			// Did the script ask us to start streaming?
			if( startStreaming )
			{
				startStreaming = false;

				// Note that this will cause the script to run again!
				return new GeneratedTextStreamingRepresentation( resource, this, executionContext, resource.getExecutionController(), executable, flushLines );

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