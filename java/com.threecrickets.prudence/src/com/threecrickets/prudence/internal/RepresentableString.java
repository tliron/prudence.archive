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

import java.util.Date;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

/**
 * Creates {@link StringRepresentation} instances on the fly from stored
 * parameters.
 * 
 * @author Tal Liron
 */
public class RepresentableString
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
	public RepresentableString( String string, MediaType mediaType, Language language, CharacterSet characterSet, Date expirationDate )
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
	public RepresentableString( String string, MediaType mediaType, Language language, CharacterSet characterSet, long expiration )
	{
		this( string, mediaType, language, characterSet, expiration > 0 ? new Date( expiration ) : null );
	}

	//
	// Attributes
	//

	/**
	 * The stored string.
	 * 
	 * @return The string
	 */
	public String getString()
	{
		return string;
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