/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.internal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentContext;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * This is the <code>prudence</code> variable exposed to scriptlets.
 * 
 * @author Tal Liron
 */
public class ExposedContainerForGeneratedTextResource
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
	public ExposedContainerForGeneratedTextResource( GeneratedTextResource resource, Representation entity, Variant variant, ConcurrentMap<String, RepresentableString> cache )
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

		documentContext = new DocumentContext( resource.getEngineManager() );
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
	 * The {@link DocumentSource} used to fetch documents.
	 * 
	 * @return The document source
	 */
	public DocumentSource<Document> getSource()
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
	 * @throws ScriptException
	 */
	public Representation includeDocument( String name ) throws IOException, ScriptException
	{
		DocumentSource.DocumentDescriptor<Document> documentDescriptor = resource.getDocumentSource().getDocumentDescriptor( name );

		Document document = documentDescriptor.getDocument();
		if( document == null )
		{
			String text = documentDescriptor.getText();
			document = new Document( text, false, resource.getEngineManager(), resource.getDefaultEngineName(), resource.getDocumentSource(), resource.isAllowCompilation() );

			Document existing = documentDescriptor.setDocumentIfAbsent( document );
			if( existing != null )
				document = existing;
		}

		if( getMediaType() == null )
		{
			// Set initial media type according to the document's tag
			setMediaType( resource.getMetadataService().getMediaType( documentDescriptor.getTag() ) );
		}

		return run( document, name );
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
	 * @throws ScriptException
	 */
	public Representation include( String name ) throws IOException, ScriptException
	{
		DocumentSource.DocumentDescriptor<Document> documentDescriptor = resource.getDocumentSource().getDocumentDescriptor( name );

		Document document = documentDescriptor.getDocument();
		if( document == null )
		{
			String scriptEngineName = ScripturianUtil.getScriptEngineNameByExtension( name, documentDescriptor.getTag(), resource.getEngineManager() );
			String text = documentDescriptor.getText();
			document = new Document( text, true, resource.getEngineManager(), scriptEngineName, resource.getDocumentSource(), resource.isAllowCompilation() );

			Document existing = documentDescriptor.setDocumentIfAbsent( document );
			if( existing != null )
				document = existing;
		}

		return run( document, name );
	}

	/**
	 * If you are in caching mode, calling this method will return true and
	 * cause the document to run again, where this next run will be in streaming
	 * mode. Whatever output the document created in the current run is
	 * discarded, and all further exceptions are ignored. For this reason, it's
	 * probably best to call <code>prudence.stream()</code> as early
	 * as possible in the document, and then to quit the document as soon as
	 * possible if it returns true. For example, your document can start by
	 * testing whether it will have a lot of output, and if so, set output
	 * characteristics, call <code>prudence.stream()</code>, and quit.
	 * If you are already in streaming mode, calling this method has no effect
	 * and returns false. Note that a good way to quit the script is to throw an
	 * exception, because it will end the script and otherwise be ignored.
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
	private final ConcurrentMap<String, RepresentableString> cache;

	/**
	 * Whether to flush the writers after every line in streaming mode.
	 */
	private boolean flushLines;

	/**
	 * The {@link MediaType} that will be used for the generated string.
	 */
	private MediaType mediaType;

	/**
	 * The {@link CharacterSet} that will be used for the generated string.
	 */
	private CharacterSet characterSet;

	/**
	 * The {@link Language} that will be used for the generated string.
	 */
	private Language language;

	/**
	 * This boolean is true when the writer is in streaming mode.
	 */
	protected boolean isStreaming;

	/**
	 * Buffer used for caching mode.
	 */
	private StringBuffer buffer;

	/**
	 * The composite script context.
	 */
	private final DocumentContext documentContext;

	/**
	 * The actual running of the document.
	 * 
	 * @param document
	 *        The document
	 * @param name
	 *        The document's name
	 * @return A representation
	 * @throws IOException
	 * @throws ScriptException
	 */
	private Representation run( Document document, String name ) throws IOException, ScriptException
	{
		boolean isStreaming = isStreaming();
		Writer writer = resource.getWriter();

		// Special handling for trivial scripts
		String trivial = document.getTrivial();
		if( trivial != null )
		{
			if( writer != null )
				writer.write( trivial );

			return new RepresentableString( trivial, getMediaType(), getLanguage(), getCharacterSet(), resource.isAllowClientCaching() ? document.getExpiration() : 0 ).represent();
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
		}

		try
		{
			// Do not allow caching in streaming mode
			PrudenceScriptletController<ExposedContainerForGeneratedTextResource> scriptletController = new PrudenceScriptletController<ExposedContainerForGeneratedTextResource>( this, resource.getContainerName(), resource.getScriptletController() );
			if( document.run( !isStreaming, writer, resource.getErrorWriter(), false, documentContext, this, scriptletController ) )
			{

				// Did the script ask us to start streaming?
				if( startStreaming )
				{
					startStreaming = false;

					// Note that this will cause the script to run again!
					return new GeneratedTextStreamingRepresentation( resource, this, documentContext, resource.getScriptletController(), document, flushLines );
				}

				if( isStreaming )
				{
					// Nothing to return in streaming mode
					return null;
				}
				else
				{
					writer.flush();

					// Get the buffer from when we ran the script
					RepresentableString string = new RepresentableString( buffer.substring( startPosition ), getMediaType(), getLanguage(), getCharacterSet(), resource.isAllowClientCaching() ? document.getExpiration()
						: 0 );

					// Cache it
					cache.put( name, string );

					// Return a representation of the entire buffer
					if( startPosition == 0 )
						return string.represent();
					else
						return new RepresentableString( buffer.toString(), getMediaType(), getLanguage(), getCharacterSet(), resource.isAllowClientCaching() ? document.getExpiration() : 0 ).represent();
				}
			}
			else
			{
				// Attempt to use cache
				RepresentableString string = cache.get( name );
				if( string != null )
				{
					if( writer != null )
						writer.write( string.getString() );

					return string.represent();
				}
				else
					return null;
			}
		}
		catch( ScriptException x )
		{
			// Did the script ask us to start streaming?
			if( startStreaming )
			{
				startStreaming = false;

				// Note that this will cause the script to run again!
				return new GeneratedTextStreamingRepresentation( resource, this, documentContext, resource.getScriptletController(), document, flushLines );

				// Note that we will allow exceptions in scripts that ask us
				// to start streaming! In fact, throwing an exception is a
				// good way for the script to signal that it's done and is
				// ready to start streaming.
			}
			else
				throw x;
		}
	}
}