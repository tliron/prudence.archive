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

import java.util.Date;
import java.util.Iterator;

import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.DelegatedResource;

/**
 * @author Tal Liron
 */
public class ExposedConversationBase<R extends ServerResource>
{
	//
	// Construction
	//

	/**
	 * @param resource
	 * @param entity
	 * @param variant
	 * @param defaultCharacterSet
	 */
	public ExposedConversationBase( R resource, Representation entity, Variant variant, CharacterSet defaultCharacterSet )
	{
		this.resource = resource;
		this.entity = entity;
		this.variant = variant;

		if( variant != null )
		{
			mediaType = variant.getMediaType();
			characterSet = variant.getCharacterSet();
		}

		if( characterSet == null )
			characterSet = defaultCharacterSet;
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
	 * The "max-age" cache control header.
	 * 
	 * @return The max age in seconds,+ or -1 if not set
	 * @see #setMaxAge(int)
	 */
	public int getMaxAge()
	{
		for( CacheDirective cacheDirective : resource.getResponse().getCacheDirectives() )
			if( cacheDirective.getName().equals( HeaderConstants.CACHE_MAX_AGE ) )
				return Integer.parseInt( cacheDirective.getValue() );

		return -1;
	}

	/**
	 * @param maxAge
	 *        The max age in seconds, or -1 to explicitly set a "no-cache" cache
	 *        control header
	 * @see #getMaxAge()
	 */
	public void setMaxAge( int maxAge )
	{
		for( Iterator<CacheDirective> i = resource.getResponse().getCacheDirectives().iterator(); i.hasNext(); )
			if( i.next().getName().equals( HeaderConstants.CACHE_MAX_AGE ) )
				i.remove();

		if( maxAge != -1 )
			resource.getResponse().getCacheDirectives().add( CacheDirective.maxAge( maxAge ) );
		else
			resource.getResponse().getCacheDirectives().add( CacheDirective.noCache() );
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
	public R getResource()
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final R resource;

	/**
	 * The {@link Variant} of this request.
	 */
	private final Variant variant;

	/**
	 * The {@link Representation} of an entity provided with this request.
	 */
	private final Representation entity;

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
	 * The {@link MediaType} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private MediaType mediaType;

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
	 * The {@link Tag} that will be used if you return an arbitrary type for
	 * <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private Tag tag;
}
