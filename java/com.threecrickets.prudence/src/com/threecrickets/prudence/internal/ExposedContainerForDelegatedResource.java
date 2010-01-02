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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentContext;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.ScriptletController;
import com.threecrickets.scripturian.exception.DocumentInitializationException;
import com.threecrickets.scripturian.exception.DocumentRunException;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * This is the <code>prudence</code> variable exposed to scriptlets.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class ExposedContainerForDelegatedResource
{
	//
	// Construction
	//

	/**
	 * Constructs a container with no variant or entity, plain text media type,
	 * and {@link DelegatedResource#getDefaultCharacterSet()}.
	 * 
	 * @param resource
	 *        The resource
	 * @param variants
	 *        The variants of the resource
	 */
	public ExposedContainerForDelegatedResource( DelegatedResource resource, List<Variant> variants )
	{
		this( resource, variants, null, null );
	}

	/**
	 * Constructs a container with media type and character set according to the
	 * entity representation, or
	 * {@link DelegatedResource#getDefaultCharacterSet()} if none is provided.
	 * 
	 * @param resource
	 *        The resource
	 * @param variants
	 *        The variants of the resource
	 * @param entity
	 *        The entity's representation
	 * @param variant
	 *        The request variant
	 */
	public ExposedContainerForDelegatedResource( DelegatedResource resource, List<Variant> variants, Representation entity, Variant variant )
	{
		this.resource = resource;
		this.variants = variants;
		this.entity = entity;
		this.variant = variant;

		if( variant != null )
		{
			mediaType = variant.getMediaType();
			characterSet = variant.getCharacterSet();
		}

		if( mediaType == null )
			mediaType = MediaType.TEXT_PLAIN;

		if( characterSet == null )
			characterSet = resource.getDefaultCharacterSet();

		documentContext = new DocumentContext( resource.getEngineManager() );
	}

	/**
	 * Constructs a container with media type and character set according to the
	 * variant, or {@link DelegatedResource#getDefaultCharacterSet()} if none is
	 * provided.
	 * 
	 * @param resource
	 *        The resource
	 * @param variants
	 *        The variants of the resource
	 * @param variant
	 *        The variant
	 */
	public ExposedContainerForDelegatedResource( DelegatedResource resource, List<Variant> variants, Variant variant )
	{
		this( resource, variants, null, variant );
	}

	//
	// Attributes
	//

	/**
	 * The {@link CharacterSet} that will be used if you return an arbitrary
	 * type for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to what the client requested (in
	 * <code>container.variant</code>), or to the value of
	 * {@link DelegatedResource#getDefaultCharacterSet()} if the client did not
	 * specify it.
	 * 
	 * @return The character set
	 * @see #setCharacterSet(CharacterSet)
	 */
	public CharacterSet getCharacterSet()
	{
		return characterSet;
	}

	/**
	 * @param characterSet
	 *        The character set
	 * @see #getCharacterSet()
	 */
	public void setCharacterSet( CharacterSet characterSet )
	{
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
	 * The {@link Language} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to null.
	 * 
	 * @return The language or null if not set
	 * @see #setLanguage(Language)
	 */
	public Language getLanguage()
	{
		return language;
	}

	/**
	 * @param language
	 *        The language or null
	 * @see #getLanguage()
	 */
	public void setLanguage( Language language )
	{
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
	 * The {@link MediaType} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to what the client requested (in
	 * <code>container.variant</code>).
	 * 
	 * @return The media type
	 * @see #setMediaType(MediaType)
	 */
	public MediaType getMediaType()
	{
		return mediaType;
	}

	/**
	 * @param mediaType
	 *        The media type
	 * @see #getMediaType()
	 */
	public void setMediaType( MediaType mediaType )
	{
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
	 * The {@link Date} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to null.
	 * 
	 * @return The date or null if not set
	 * @see #setExpirationDate(Date)
	 */
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	/**
	 * @param expirationDate
	 *        The date or null
	 * @see #getExpirationDate()
	 */
	public void setExpirationDate( Date expirationDate )
	{
		this.expirationDate = expirationDate;
	}

	/**
	 * @return The timestamp or 0 if not set
	 * @see #setExpirationDate()
	 */
	public long getExpirationTimestamp()
	{
		return expirationDate != null ? expirationDate.getTime() : 0L;
	}

	/**
	 * @param expirationTimestamp
	 *        The timestamp
	 * @see #setExpirationDate(Date)
	 */
	public void setExpirationTimestamp( long expirationTimestamp )
	{
		this.expirationDate = new Date( expirationTimestamp );
	}

	/**
	 * The {@link Date} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to null.
	 * 
	 * @return The date or null if not set
	 * @see #setModificationDate(Date)
	 */
	public Date getModificationDate()
	{
		return modificationDate;
	}

	/**
	 * @param modificationDate
	 *        The date or null
	 * @see #getModificationDate()
	 */
	public void setModificationDate( Date modificationDate )
	{
		this.modificationDate = modificationDate;
	}

	/**
	 * @return The timestamp or 0 if not set
	 * @see #setModificationDate()
	 */
	public long getModificationTimestamp()
	{
		return modificationDate != null ? modificationDate.getTime() : 0L;
	}

	/**
	 * @param modificationTimestamp
	 *        The timestamp
	 * @see #setModificationDate(Date)
	 */
	public void setModificationTimestamp( long modificationTimestamp )
	{
		this.modificationDate = modificationTimestamp != 0 ? new Date( modificationTimestamp ) : null;
	}

	/**
	 * The {@link Tag} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to null.
	 * 
	 * @return The tag or null if not set
	 * @see #setTag(Tag)
	 */
	public Tag getTag()
	{
		return tag;
	}

	/**
	 * @param tag
	 *        The tag or null
	 * @see #getTag()
	 */
	public void setTag( Tag tag )
	{
		this.tag = tag;
	}

	/**
	 * @return The HTTP-formatted tag or null if not set
	 * @see #getTag()
	 */
	public String getHttpTag()
	{
		return tag != null ? tag.format() : null;
	}

	/**
	 * @param tag
	 *        The HTTP-formatted tag or null
	 * @see #setTag(Tag)
	 */
	public void setHttpTag( String tag )
	{
		this.tag = tag != null ? Tag.parse( tag ) : null;
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
	 * For example, during a call to <code>handleInit()</code>, this can be used
	 * to change the characteristics of the resource. Otherwise, you can use it
	 * to access the request and response.
	 * 
	 * @return The resource
	 */
	public DelegatedResource getResource()
	{
		return resource;
	}

	/**
	 * The {@link Variant} of this request. Useful for interrogating the
	 * client's preferences. This is available only in <code>handleGet()</code>,
	 * <code>handlePost()</code> and <code>handlePut()</code>.
	 * 
	 * @return The variant or null if not available
	 */
	public Variant getVariant()
	{
		return variant;
	}

	/**
	 * A map of possible variants or media types supported by this resource. You
	 * should initialize this during a call to <code>handleInit()</code> .
	 * Values for the map can be {@link MediaType} constants, explicit
	 * {@link Variant} instances (in which case these variants will be returned
	 * immediately for their media type without calling the entry point), or a
	 * {@link List} containing both media types and variants. Use map key
	 * {@link Method#ALL} to indicate support for all methods.
	 * 
	 * @return The variants
	 */
	public List<Variant> getVariants()
	{
		return variants;
	}

	/**
	 * The {@link Representation} of an entity provided with this request.
	 * Available only in <code>handlePost()</code> and <code>handlePut()</code>.
	 * Note that <code>container.variant</code> is identical to
	 * <code>container.entity</code> when available.
	 * 
	 * @return The entity's representation or null if not available
	 */
	public Representation getEntity()
	{
		return entity;
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

	//
	// Operations
	//

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaType
	 *        The media type
	 * @see #getVariants()
	 */
	public void addMediaType( MediaType mediaType )
	{
		resource.getVariants().add( new Variant( mediaType ) );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaTypeName
	 *        The media type name
	 * @see #getVariants()
	 */
	public void addMediaTypeByName( String mediaTypeName )
	{
		resource.getVariants().add( new Variant( MediaType.valueOf( mediaTypeName ) ) );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaTypeExtension
	 *        The media type extension
	 * @see #getVariants()
	 */
	public void addMediaTypeByExtension( String mediaTypeExtension )
	{
		resource.getVariants().add( new Variant( resource.getApplication().getMetadataService().getMediaType( mediaTypeExtension ) ) );
	}

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
	 * @throws IOException
	 * @throws DocumentInitializationException
	 * @throws DocumentRunException
	 */
	public void includeDocument( String name ) throws IOException, DocumentInitializationException, DocumentRunException
	{
		DocumentSource.DocumentDescriptor<Document> documentDescriptor = resource.getDocumentSource().getDocumentDescriptor( name );

		Document document = documentDescriptor.getDocument();
		if( document == null )
		{
			String text = documentDescriptor.getText();
			document = new Document( name, text, false, this.resource.getEngineManager(), resource.getDefaultEngineName(), resource.getDocumentSource(), resource.isAllowCompilation() );

			Document existing = documentDescriptor.setDocumentIfAbsent( document );
			if( existing != null )
				document = existing;
		}

		PrudenceScriptletController<ExposedContainerForDelegatedResource> scriptletController = new PrudenceScriptletController<ExposedContainerForDelegatedResource>( this, resource.getContainerName(), resource
			.getScriptletController() );
		document.run( false, resource.getWriter(), resource.getErrorWriter(), true, documentContext, this, scriptletController );
	}

	/**
	 * As {@link #includeDocument(String)}, except that the document is parsed
	 * as a single, non-delimited script with the engine name derived from
	 * name's extension.
	 * 
	 * @param name
	 *        The script name
	 * @throws IOException
	 * @throws DocumentInitializationException
	 * @throws DocumentRunException
	 */
	public void include( String name ) throws IOException, DocumentInitializationException, DocumentRunException
	{
		DocumentSource.DocumentDescriptor<Document> documentDescriptor = resource.getDocumentSource().getDocumentDescriptor( name );

		Document document = documentDescriptor.getDocument();
		if( document == null )
		{
			String scriptEngineName = ScripturianUtil.getScriptEngineNameByExtension( name, documentDescriptor.getTag(), resource.getEngineManager() );
			String text = documentDescriptor.getText();
			document = new Document( name, text, true, resource.getEngineManager(), scriptEngineName, resource.getDocumentSource(), resource.isAllowCompilation() );

			Document existing = documentDescriptor.setDocumentIfAbsent( document );
			if( existing != null )
				document = existing;
		}

		PrudenceScriptletController<ExposedContainerForDelegatedResource> scriptletController = new PrudenceScriptletController<ExposedContainerForDelegatedResource>( this, resource.getContainerName(), resource
			.getScriptletController() );
		document.run( false, resource.getWriter(), resource.getErrorWriter(), true, documentContext, this, scriptletController );
	}

	/**
	 * Invokes an entry point in the document.
	 * 
	 * @param entryPointName
	 *        Name of entry point
	 * @return Result of invocation
	 * @throws ResourceException
	 * @see {@link Document#invoke(String, Object, ScriptletController)}
	 */
	public Object invoke( String entryPointName ) throws ResourceException
	{
		String name = PrudenceUtils.getRemainingPart( resource.getRequest(), resource.getDefaultName() );

		try
		{
			DocumentSource.DocumentDescriptor<Document> documentDescriptor = resource.getDocumentSource().getDocumentDescriptor( name );
			PrudenceScriptletController<ExposedContainerForDelegatedResource> scriptletController = new PrudenceScriptletController<ExposedContainerForDelegatedResource>( this, resource.getContainerName(), resource
				.getScriptletController() );

			Document document = documentDescriptor.getDocument();
			if( document == null )
			{
				String scriptEngineName = ScripturianUtil.getScriptEngineNameByExtension( name, documentDescriptor.getTag(), resource.getEngineManager() );
				String text = documentDescriptor.getText();
				document = new Document( name, text, true, resource.getEngineManager(), scriptEngineName, resource.getDocumentSource(), resource.isAllowCompilation() );
				Document existing = documentDescriptor.setDocumentIfAbsent( document );

				if( existing != null )
					document = existing;
				else
				{
					// Must run document once and only once
					document.run( false, resource.getWriter(), resource.getErrorWriter(), true, documentContext, this, scriptletController );
				}
			}

			return document.invoke( entryPointName, this, scriptletController );
		}
		catch( FileNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}
		catch( DocumentInitializationException x )
		{
			throw new ResourceException( x );
		}
		catch( DocumentRunException x )
		{
			throw new ResourceException( x );
		}
		catch( NoSuchMethodException x )
		{
			throw new ResourceException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The instance of this resource.
	 */
	private final DelegatedResource resource;

	/**
	 * The variants of this resource.
	 */
	private final List<Variant> variants;

	/**
	 * The {@link Variant} of this request.
	 */
	private final Variant variant;

	/**
	 * The {@link Representation} of an entity provided with this request.
	 */
	private final Representation entity;

	/**
	 * The {@link MediaType} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private MediaType mediaType;

	/**
	 * The {@link CharacterSet} that will be used if you return an arbitrary
	 * type for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private CharacterSet characterSet;

	/**
	 * The {@link Language} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private Language language;

	/**
	 * The {@link Tag} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private Tag tag;

	/**
	 * The {@link Date} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private Date expirationDate;

	/**
	 * The {@link Date} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private Date modificationDate;

	/**
	 * The composite script context.
	 */
	private final DocumentContext documentContext;
}