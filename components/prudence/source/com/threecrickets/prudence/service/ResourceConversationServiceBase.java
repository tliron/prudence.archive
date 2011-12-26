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

import java.io.File;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 */
public class ResourceConversationServiceBase<R extends ServerResource> extends ConversationService
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param entity
	 *        The client entity or null
	 * @param preferences
	 *        The negotiated client preferences or null
	 * @param defaultCharacterSet
	 *        The character set to use if unspecified by variant
	 * @param supportedEncodings
	 *        The supported encodings or null
	 * @param fileUploadSizeThreshold
	 *        The size in bytes beyond which uploaded files will be stored to
	 *        disk
	 * @param fileUploadDirectory
	 *        The directory in which to place uploaded files
	 */
	public ResourceConversationServiceBase( R resource, Representation entity, Variant preferences, CharacterSet defaultCharacterSet, List<Encoding> supportedEncodings, int fileUploadSizeThreshold,
		File fileUploadDirectory )
	{
		super( fileUploadSizeThreshold, fileUploadDirectory );

		this.resource = resource;
		this.entity = entity;
		this.preferences = preferences != null ? preferences : getPreferredVariant();

		if( preferences != null )
		{
			mediaType = preferences.getMediaType();
			characterSet = preferences.getCharacterSet();

			if( supportedEncodings != null )
			{
				List<Encoding> preferredEncodings = preferences.getEncodings();
				for( Encoding encoding : supportedEncodings )
				{
					if( preferredEncodings.contains( encoding ) )
					{
						this.encoding = encoding;
						break;
					}
				}
			}
		}

		// For HTML forms, switch to HTML
		// if( entity != null && ( entity.getMediaType().equals(
		// MediaType.APPLICATION_WWW_FORM ) ) )
		// mediaType = MediaType.TEXT_HTML;

		if( characterSet == null )
			characterSet = defaultCharacterSet;
	}

	//
	// Attributes
	//

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
		return characterSet != null ? resource.getMetadataService().getExtension( characterSet ) : null;
	}

	/**
	 * @param characterSetShortName
	 *        The character set short name
	 * @see #setCharacterSet(CharacterSet)
	 */
	public void setCharacterSetShortName( String characterSetShortName )
	{
		characterSet = resource.getMetadataService().getCharacterSet( characterSetShortName );
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
		return mediaType != null ? resource.getMetadataService().getExtension( mediaType ) : null;
	}

	/**
	 * @param mediaTypeExtension
	 *        The media type extension
	 * @see #setMediaType(MediaType)
	 */
	public void setMediaTypeExtension( String mediaTypeExtension )
	{
		mediaType = resource.getMetadataService().getMediaType( mediaTypeExtension );
	}

	/**
	 * The preferred variant.
	 * 
	 * @return The preferred variant
	 */
	public Variant getPreferredVariant()
	{
		if( preferredVariant == null )
			preferredVariant = resource.getRequest().getClientInfo().getPreferredVariant( resource.getVariants(), resource.getMetadataService() );
		return preferredVariant;
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
	 * The negotiated client preferences.
	 * 
	 * @return The negotiated preferences or null if not available
	 */
	public Variant getPreferences()
	{
		return preferences;
	}

	/**
	 * The client entity.
	 * 
	 * @return The client entity's representation or null if not available
	 */
	public Representation getEntity()
	{
		return entity;
	}

	//
	// ConversationServiceBase
	//

	/**
	 * A shortcut to the resource request.
	 * 
	 * @return The request
	 */
	@Override
	public Request getRequest()
	{
		return getResource().getRequest();
	}

	/**
	 * A shortcut to the resource response.
	 * 
	 * @return The response
	 */
	@Override
	public Response getResponse()
	{
		return getResource().getResponse();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final R resource;

	/**
	 * The negotiated client preferences.
	 */
	private final Variant preferences;

	/**
	 * The client entity.
	 */
	private final Representation entity;

	/**
	 * The encoding.
	 */
	private Encoding encoding;

	/**
	 * The character set.
	 */
	private CharacterSet characterSet;

	/**
	 * The language.
	 */
	private Language language;

	/**
	 * The media type.
	 */
	private MediaType mediaType;

	/**
	 * The preferred variant
	 */
	private Variant preferredVariant;
}
