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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.util.CaptiveRedirector;
import com.threecrickets.prudence.util.ConversationCookie;
import com.threecrickets.prudence.util.FileParameter;
import com.threecrickets.prudence.util.FormWithFiles;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 */
public class ConversationServiceBase<R extends ServerResource>
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param resource
	 *        The resource
	 * @param entity
	 *        The entity or null
	 * @param variant
	 *        The variant or null
	 * @param defaultCharacterSet
	 *        The character set to use if unspecified by variant
	 */
	public ConversationServiceBase( R resource, Representation entity, Variant variant, CharacterSet defaultCharacterSet )
	{
		this.resource = resource;
		this.entity = entity;
		this.variant = variant;

		if( variant != null )
		{
			mediaType = variant.getMediaType();
			characterSet = variant.getCharacterSet();
		}

		// For HTML forms, switch to HTML
		if( entity != null && ( entity.getMediaType().equals( MediaType.APPLICATION_WWW_FORM ) ) )
			mediaType = MediaType.TEXT_HTML;

		if( characterSet == null )
			characterSet = defaultCharacterSet;
	}

	//
	// Attributes
	//

	/**
	 * The resource reference.
	 * 
	 * @return The reference
	 */
	public Reference getReference()
	{
		return resource.getReference();
	}

	/**
	 * The cookies.
	 * 
	 * @return The cookies
	 */
	public Collection<ConversationCookie> getCookies()
	{
		if( conversationCookies == null )
			conversationCookies = ConversationCookie.wrapCookies( resource );
		return conversationCookies;
	}

	/**
	 * Returns a new cookie instance if the cookie doesn't exist yet, or the
	 * existing cookie if it does. Note that the cookie will not be saved into
	 * the response until you call {@link ConversationCookie#save()}.
	 * 
	 * @param name
	 *        The cookie name
	 * @return A new cookie or the existing cookie
	 */
	public ConversationCookie createCookie( String name )
	{
		return ConversationCookie.createCookie( name, resource.getCookieSettings(), getCookies() );
	}

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
	 * @return The character set short name
	 * @see #getCharacterSet()
	 */
	public String getCharacterSetShortName()
	{
		return characterSet != null ? resource.getApplication().getMetadataService().getExtension( characterSet ) : null;
	}

	/**
	 * @param characterSetShortName
	 *        The character set short name
	 * @see #setCharacterSet(CharacterSet)
	 */
	public void setCharacterSetShortName( String characterSetShortName )
	{
		characterSet = resource.getApplication().getMetadataService().getCharacterSet( characterSetShortName );
	}

	/**
	 * The {@link Encoding} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>. Defaults to null.
	 * 
	 * @return The encoding or null if not set
	 * @see #setEncoding(Language)
	 */
	public Encoding getEncoding()
	{
		return encoding;
	}

	/**
	 * @param encoding
	 *        The encoding or null
	 * @see #getEncoding()
	 */
	public void setEncoding( Encoding encoding )
	{
		this.encoding = encoding;
	}

	/**
	 * @return The encoding name
	 * @see #getEncoding()
	 */
	public String getEncodingName()
	{
		return encoding != null ? encoding.getName() : null;
	}

	/**
	 * @param encodingName
	 *        The encoding name
	 * @see #setEncoding(Encoding)
	 */
	public void setEncodingName( String encodingName )
	{
		encoding = Encoding.valueOf( encodingName );
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
	 * The response status.
	 * 
	 * @return The response status
	 * @see #setStatus(Status)
	 */
	public Status getStatus()
	{
		return resource.getResponse().getStatus();
	}

	/**
	 * The response status.
	 * 
	 * @param status
	 *        The response status
	 * @see #getStatus()
	 */
	public void setStatus( Status status )
	{
		resource.getResponse().setStatus( status );
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
	 * A shortcut to the resource request.
	 * 
	 * @return The request
	 */
	public Request getRequest()
	{
		return resource.getRequest();
	}

	/**
	 * A shortcut to the resource response.
	 * 
	 * @return The response
	 */
	public Response getResponse()
	{
		return resource.getResponse();
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

	/**
	 * The relative path that would reach the base URI of the application if
	 * appended to the current resource URI.
	 * 
	 * @return The relative path
	 */
	public String getPathToBase()
	{
		Request request = resource.getRequest();
		Reference reference = CaptiveRedirector.getCaptiveReference( request );
		if( reference == null )
			reference = request.getResourceRef();

		// Reverse relative reference
		String relative = reference.getBaseRef().getRelativeRef( reference ).getPath();

		return relative;
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
	 * Throws a runtime exception.
	 * 
	 * @return Always throws an exception, so nothing is ever returned (some
	 *         templating languages require a return value anyway)
	 */
	public boolean stop()
	{
		throw new RuntimeException( "conversation.stop was called" );
	}

	/**
	 * The URI query as a list. Includes duplicate keys.
	 * <p>
	 * The value is internally cached, so it's cheap to call this repeatedly.
	 * 
	 * @return The query form
	 */
	public Form getQueryAll()
	{
		if( queryAll == null )
			queryAll = resource.getRequest().getResourceRef().getQueryAsForm();
		return queryAll;
	}

	/**
	 * The URI query as a map. In the case of duplicate keys, only the last one
	 * will appear.
	 * <p>
	 * The value is internally cached, so it's cheap to call this repeatedly.
	 * 
	 * @return The query map
	 */
	public Map<String, String> getQuery()
	{
		if( query == null )
			query = getQueryAll().getValuesMap();
		return query;
	}

	/**
	 * The form, sent via POST or PUT, as a list. Includes duplicate keys.
	 * Uploaded files will appear as instances of {@link FileParameter}.
	 * <p>
	 * The value is internally cached, so it's cheap to call this repeatedly.
	 * 
	 * @return The form
	 */
	public Form getFormAll()
	{
		if( formAll == null )
		{
			if( resource.getRequest().isEntityAvailable() )
				formAll = new FormWithFiles( resource.getRequestEntity() );
			else
				formAll = new Form();
		}
		return formAll;
	}

	/**
	 * The form, sent via POST or PUT, as a map. In the case of duplicate keys,
	 * only the last one will appear. Uploaded files will appear as instances of
	 * {@link FileParameter}. Other fields will be plain strings.
	 * <p>
	 * The value is internally cached, so it's cheap to call this repeatedly.
	 * 
	 * @return The form
	 */
	public Map<String, Object> getForm()
	{
		if( form == null )
		{
			form = new HashMap<String, Object>();
			for( Parameter parameter : getFormAll() )
			{
				if( parameter instanceof FileParameter )
					form.put( parameter.getName(), parameter );
				else
					form.put( parameter.getName(), parameter.getValue() );
			}
		}
		return form;
	}

	/**
	 * The conversation locals include the URI template variables.
	 * 
	 * @return The locals
	 */
	public Map<String, Object> getLocals()
	{
		return resource.getRequestAttributes();
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
	 * The {@link Encoding} that will be used if you return an arbitrary type
	 * for <code>handleGet()</code>, <code>handlePost()</code> and
	 * <code>handlePut()</code>.
	 */
	private Encoding encoding;

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

	/**
	 * The URI query as a map.
	 */
	private Map<String, String> query;

	/**
	 * The URI query as a list.
	 */
	private Form queryAll;

	/**
	 * The form, sent via POST or PUT, as a map.
	 */
	private Form formAll;

	/**
	 * The form, sent via POST or PUT, as a list.
	 */
	private Map<String, Object> form;

	/**
	 * The conversation cookies.
	 */
	private Collection<ConversationCookie> conversationCookies;
}
