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
	 */
	public RepresentableString( String string, MediaType mediaType, Language language, CharacterSet characterSet )
	{
		this.string = string;
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
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
		return this.string;
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
		return new StringRepresentation( this.string, this.mediaType, this.language, this.characterSet );
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
}