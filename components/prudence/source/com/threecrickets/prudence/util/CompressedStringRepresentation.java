/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

/**
 * This is a {@link ByteArrayRepresentation} that can be constructed using text
 * and an encoding, which it then compresses into bytes according the encoding.
 * 
 * @author Tal Liron
 */
public class CompressedStringRepresentation extends ByteArrayRepresentation
{
	//
	// Construction
	//

	/**
	 * Construction with media type "text/plain" and UTF-8.
	 * 
	 * @param text
	 *        The text
	 * @param encoding
	 *        The encoding
	 * @throws IOException
	 */
	public CompressedStringRepresentation( CharSequence text, Encoding encoding ) throws IOException
	{
		this( text, MediaType.TEXT_PLAIN, encoding );
	}

	/**
	 * Construction with media type "text/plain" and UTF-8.
	 * 
	 * @param text
	 *        The text
	 * @param language
	 *        The language
	 * @param encoding
	 *        The encoding
	 * @throws IOException
	 */
	public CompressedStringRepresentation( CharSequence text, Language language, Encoding encoding ) throws IOException
	{
		this( text, MediaType.TEXT_PLAIN, language, encoding );
	}

	/**
	 * Construction with UTF-8.
	 * 
	 * @param text
	 *        The text
	 * @param mediaType
	 *        The media type
	 * @param encoding
	 *        The encoding
	 * @throws IOException
	 */
	public CompressedStringRepresentation( CharSequence text, MediaType mediaType, Encoding encoding ) throws IOException
	{
		this( text, mediaType, null, encoding );
	}

	/**
	 * Construction with UTF-8.
	 * 
	 * @param text
	 *        The text
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param encoding
	 *        The encoding
	 * @throws IOException
	 */
	public CompressedStringRepresentation( CharSequence text, MediaType mediaType, Language language, Encoding encoding ) throws IOException
	{
		this( text, mediaType, language, CharacterSet.UTF_8, encoding );
	}

	/**
	 * @param text
	 *        The text
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @throws IOException
	 */
	public CompressedStringRepresentation( CharSequence text, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding ) throws IOException
	{
		super( mediaType, IoUtil.compress( text, encoding, "text" ) );
		if( language != null )
			getLanguages().add( language );
		setCharacterSet( characterSet );
		getEncodings().add( encoding );
	}
}
