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

package com.threecrickets.prudence.cache;

import java.io.Serializable;
import java.util.Date;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

/**
 * A serializable, cacheable set of parameters from which
 * {@link StringRepresentation} instances can be created.
 * 
 * @author Tal Liron
 * @see Cache
 */
public class CacheEntry implements Serializable
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param string
	 *        The string
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param expirationDate
	 *        The expiration date
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, Date expirationDate )
	{
		this.string = string;
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
		this.expirationDate = expirationDate;
	}

	/**
	 * Constructor.
	 * 
	 * @param string
	 *        The string
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param expiration
	 *        Expiration timestamp or 0
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, long expiration )
	{
		this( string, mediaType, language, characterSet, expiration > 0 ? new Date( expiration ) : null );
	}

	//
	// Attributes
	//

	/**
	 * @return The string
	 */
	public String getString()
	{
		return string;
	}

	/**
	 * @return The media type
	 */
	public MediaType getMediaType()
	{
		return mediaType;
	}

	/**
	 * @return The language
	 */
	public Language getLanguage()
	{
		return language;
	}

	/**
	 * @return The character set
	 */
	public CharacterSet getCharacterSet()
	{
		return characterSet;
	}

	/**
	 * @return The modification date
	 */
	public Date getModificationDate()
	{
		return modificationDate;
	}

	/**
	 * @return The expiration date
	 */
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	//
	// Operations
	//

	/**
	 * Creates a {@link StringRepresentation}.
	 * 
	 * @return A {@link StringRepresentation}
	 */
	public StringRepresentation represent()
	{
		StringRepresentation representation = new StringRepresentation( string, mediaType, language, characterSet );
		representation.setModificationDate( modificationDate );
		representation.setExpirationDate( expirationDate );
		return representation;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * For the Serializable interface.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The stored string.
	 */
	private final String string;

	/**
	 * The media type.
	 */
	private final MediaType mediaType;

	/**
	 * The language.
	 */
	private final Language language;

	/**
	 * The character set.
	 */
	private final CharacterSet characterSet;

	/**
	 * The modification date.
	 */
	private final Date modificationDate = new Date();

	/**
	 * The expiration date.
	 */
	private final Date expirationDate;
}