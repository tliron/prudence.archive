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

import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentContext;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.ScriptletController;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * This is the <code>document.container</code> variable exposed to scriptlets.
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
		this.resource = resource;
		this.variants = variants;
		variant = null;
		entity = null;
		mediaType = MediaType.TEXT_PLAIN;
		characterSet = resource.getDefaultCharacterSet();
		documentContext = new DocumentContext( resource.getEngineManager() );
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
		mediaType = variant.getMediaType();
		characterSet = this.variant.getCharacterSet();
		if( characterSet == null )
		{
			characterSet = resource.getDefaultCharacterSet();
		}
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
		this.resource = resource;
		this.variants = variants;
		this.variant = variant;
		entity = null;

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
	 * @return The date or 0 if not set
	 * @see #setExpirationDate()
	 */
	public long getExpirationDateAsLong()
	{
		return expirationDate != null ? expirationDate.getTime() : 0L;
	}

	/**
	 * @param expirationDate
	 *        The date
	 * @see #setExpirationDate(Date)
	 */
	public void setExpirationDateAsLong( long expirationDate )
	{
		this.expirationDate = new Date( expirationDate );
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
	 * @return The date or 0 if not set
	 * @see #setModificationDate()
	 */
	public long getModificationDateAsLong()
	{
		return modificationDate != null ? modificationDate.getTime() : 0L;
	}

	/**
	 * @param modificationDate
	 *        The date
	 * @see #setModificationDate(Date)
	 */
	public void setModificationDateAsLong( long modificationDate )
	{
		this.modificationDate = new Date( modificationDate );
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
	public String getTagAsString()
	{
		return tag != null ? tag.format() : null;
	}

	/**
	 * @param tag
	 *        The HTTP-formatted tag or null
	 * @see #setTag(Tag)
	 */
	public void setTagAsString( String tag )
	{
		this.tag = tag != null ? Tag.parse( tag ) : null;
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
	 * Returns a representation based on the object. If the object is not
	 * already a representation, creates a new representation based on the
	 * container's attributes.
	 * 
	 * @param object
	 *        An object
	 * @return A representation
	 */
	public Representation getRepresentation( Object object )
	{
		if( object == null )
			return null;
		else if( object instanceof Representation )
			return (Representation) object;
		else
		{
			Representation representation = new StringRepresentation( object.toString(), getMediaType(), getLanguage(), getCharacterSet() );
			representation.setTag( getTag() );
			representation.setExpirationDate( getExpirationDate() );
			representation.setModificationDate( getModificationDate() );
			return representation;
		}
	}

	/**
	 * Returns a representation info based on the object. If the object is not
	 * already a representation info, creates a new representation info based on
	 * the container's attributes.
	 * 
	 * @param object
	 *        An object
	 * @return A representation info
	 */
	public RepresentationInfo getRepresentationInfo( Object object )
	{
		if( object == null )
			return null;
		else if( object instanceof RepresentationInfo )
			return (RepresentationInfo) object;
		else if( object instanceof Date )
			return new RepresentationInfo( getMediaType(), (Date) object );
		else if( object instanceof Number )
			return new RepresentationInfo( getMediaType(), new Date( ( (Number) object ).longValue() ) );
		else if( object instanceof Tag )
			return new RepresentationInfo( getMediaType(), (Tag) object );
		else if( object instanceof String )
			return new RepresentationInfo( getMediaType(), Tag.parse( (String) object ) );
		else
			throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "cannot convert " + object.getClass().toString() + " to a RepresentationInfo" );
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
	 * @throws ScriptException
	 */
	public void includeDocument( String name ) throws IOException, ScriptException
	{
		DocumentSource.DocumentDescriptor<Document> documentDescriptor = resource.getDocumentSource().getDocumentDescriptor( name );

		Document document = documentDescriptor.getDocument();
		if( document == null )
		{
			String text = documentDescriptor.getText();
			document = new Document( text, false, this.resource.getEngineManager(), resource.getDefaultEngineName(), resource.getDocumentSource(), resource.isAllowCompilation() );

			Document existing = documentDescriptor.setDocumentIfAbsent( document );
			if( existing != null )
				document = existing;
		}

		document.run( false, resource.getWriter(), resource.getErrorWriter(), true, documentContext, this, resource.getScriptletController() );
	}

	/**
	 * As {@link #includeDocument(String)}, except that the document is parsed
	 * as a single, non-delimited script with the engine name derived from
	 * name's extension.
	 * 
	 * @param name
	 *        The script name
	 * @throws IOException
	 * @throws ScriptException
	 */
	public void include( String name ) throws IOException, ScriptException
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

		document.run( false, resource.getWriter(), resource.getErrorWriter(), true, documentContext, this, resource.getScriptletController() );
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

			Document document = documentDescriptor.getDocument();
			if( document == null )
			{
				String scriptEngineName = ScripturianUtil.getScriptEngineNameByExtension( name, documentDescriptor.getTag(), resource.getEngineManager() );
				String text = documentDescriptor.getText();
				document = new Document( text, true, resource.getEngineManager(), scriptEngineName, resource.getDocumentSource(), resource.isAllowCompilation() );
				Document existing = documentDescriptor.setDocumentIfAbsent( document );

				if( existing != null )
					document = existing;
				else
					// Must run document once and only once
					document.run( false, resource.getWriter(), resource.getErrorWriter(), true, documentContext, this, resource.getScriptletController() );
			}

			return document.invoke( entryPointName, this, resource.getScriptletController() );
		}
		catch( FileNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}
		catch( ScriptException x )
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