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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
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
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

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
	 * @param fileUploadSizeThreshold
	 *        The size in bytes beyond which uploaded files will be stored to
	 *        disk
	 * @param fileUploadDirectory
	 *        The directory in which to place uploaded files
	 * @param defaultCharacterSet
	 *        The character set to use if unspecified by variant
	 */
	public ConversationServiceBase( R resource, Representation entity, Variant variant, CharacterSet defaultCharacterSet, int fileUploadSizeThreshold, File fileUploadDirectory )
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

		this.fileUploadSizeThreshold = fileUploadSizeThreshold;
		this.fileUploadDirectory = fileUploadDirectory;
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
		Request request = resource.getRequest();
		Reference reference = CaptiveRedirector.getCaptiveReference( request );
		if( reference == null )
			reference = request.getResourceRef();
		return reference;
	}

	/**
	 * The conversation cookies.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The conversation cookies
	 */
	public Collection<ConversationCookie> getCookies()
	{
		if( conversationCookies == null )
			conversationCookies = ConversationCookie.wrapCookies( resource );
		return conversationCookies;
	}

	/**
	 * Gets a conversation cookie by name.
	 * 
	 * @return The conversation cookie or null
	 */
	public ConversationCookie getCookie( String name )
	{
		for( ConversationCookie cookie : getCookies() )
			if( cookie.getName().equals( name ) )
				return cookie;
		return null;
	}

	/**
	 * Returns a new conversation cookie instance if the cookie doesn't exist
	 * yet, or the existing cookie if it does. Note that the cookie will not be
	 * saved into the response until you call {@link ConversationCookie#save()}.
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
	 * The character set.
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
	 * The encoding.
	 * 
	 * @return The encoding or null if not set
	 * @see #setEncoding(Encoding)
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
	 * The language.
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
	 * The media type.
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
	 * The resource.
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
	 * The variant.
	 * 
	 * @return The variant or null if not available
	 */
	public Variant getVariant()
	{
		return variant;
	}

	/**
	 * The entity.
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
	 * This value is cached locally.
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
	 * This value is cached locally.
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
	 * This value is cached locally.
	 * 
	 * @return The form
	 */
	public Form getFormAll()
	{
		if( formAll == null )
		{
			if( resource.getRequest().isEntityAvailable() )
				formAll = new FormWithFiles( resource.getRequestEntity(), fileUploadSizeThreshold, fileUploadDirectory );
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
	 * This value is cached locally.
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
	 * The request attributes.
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
	 * The entity.
	 */
	private final Representation entity;

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	private final int fileUploadSizeThreshold;

	/**
	 * The directory in which to place uploaded files.
	 */
	private final File fileUploadDirectory;

	/**
	 * The character set.
	 */
	private CharacterSet characterSet;

	/**
	 * The encoding.
	 */
	private Encoding encoding;

	/**
	 * The language.
	 */
	private Language language;

	/**
	 * The media type.
	 */
	private MediaType mediaType;

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
