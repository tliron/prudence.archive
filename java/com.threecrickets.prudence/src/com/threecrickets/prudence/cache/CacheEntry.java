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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;

import com.threecrickets.prudence.util.ByteArrayRepresentation;
import com.threecrickets.prudence.util.IoUtil;

/**
 * A serializable, cacheable set of parameters from which
 * {@link StringRepresentation} or {@link ByteArrayRepresentation} instances can
 * be created.
 * <p>
 * Instances are not thread safe.
 * 
 * @author Tal Liron
 * @see Cache
 */
public class CacheEntry implements Externalizable
{
	//
	// Construction
	//

	/**
	 * Constructor. A constructor without arguments is required for
	 * {@link Externalizable}.
	 */
	public CacheEntry()
	{
	}

	/**
	 * Construction. Compresses string if encoding is provided.
	 * 
	 * @param string
	 *        The string
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param documentModificationDate
	 *        The document modification date
	 * @param expirationDate
	 *        The expiration date
	 * @throws IOException
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, Date documentModificationDate, Date expirationDate ) throws IOException
	{
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
		this.encoding = encoding;
		this.documentModificationDate = documentModificationDate;
		this.expirationDate = expirationDate;

		if( IoUtil.SUPPORTED_COMPRESSION_ENCODINGS.contains( encoding ) )
		{
			bytes = IoUtil.compress( string, encoding, "text" );
		}
		else
			this.string = string;
	}

	/**
	 * Construction.
	 * 
	 * @param bytes
	 *        The bytes
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param documentModificationDate
	 *        The document modification date
	 * @param expirationDate
	 *        The expiration date
	 */
	public CacheEntry( byte[] bytes, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, Date documentModificationDate, Date expirationDate )
	{
		this.bytes = bytes;
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
		this.encoding = encoding;
		this.documentModificationDate = documentModificationDate;
		this.expirationDate = expirationDate;
	}

	/**
	 * Construction.
	 * 
	 * @param string
	 *        The string
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param docmuentModificationTimestamp
	 *        The document modification timestamp
	 * @param expirationTimestamp
	 *        The expiration timestamp or 0 for no expiration
	 * @throws IOException
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, long docmuentModificationTimestamp, long expirationTimestamp ) throws IOException
	{
		this( string, mediaType, language, characterSet, encoding, new Date( docmuentModificationTimestamp ), expirationTimestamp > 0 ? new Date( expirationTimestamp ) : null );
	}

	/**
	 * Construction.
	 * 
	 * @param bytes
	 *        The bytes
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param docmuentModificationTimestamp
	 *        The document modification timestamp
	 * @param expirationTimestamp
	 *        The expiration timestamp or 0 for no expiration
	 * @throws IOException
	 */
	public CacheEntry( byte[] bytes, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, long docmuentModificationTimestamp, long expirationTimestamp )
	{
		this( bytes, mediaType, language, characterSet, encoding, new Date( docmuentModificationTimestamp ), expirationTimestamp > 0 ? new Date( expirationTimestamp ) : null );
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
	 * @return The bytes
	 */
	public byte[] getBytes()
	{
		return bytes;
	}

	/**
	 * @return The length in bytes of either the string or the bytes
	 */
	public int getSize()
	{
		if( string != null )
			return string.getBytes().length;
		else
			return bytes.length;
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
	 * @return The encoding
	 */
	public Encoding getEncoding()
	{
		return encoding;
	}

	/**
	 * @return The document modification date
	 */
	public Date getDocumentModificationDate()
	{
		return documentModificationDate;
	}

	/**
	 * @return The entry modification date
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
	 * Creates a {@link StringRepresentation} or a
	 * {@link ByteArrayRepresentation}.
	 * 
	 * @return A {@link Representation}
	 */
	public Representation represent()
	{
		Representation representation;

		if( bytes != null )
		{
			representation = new ByteArrayRepresentation( mediaType, bytes );
			if( language != null )
				representation.getLanguages().add( language );
			representation.setCharacterSet( characterSet );
			if( encoding != null )
				representation.getEncodings().add( encoding );
		}
		else
			representation = new StringRepresentation( string, mediaType, language, characterSet );

		representation.setModificationDate( modificationDate );
		representation.setExpirationDate( expirationDate );
		return representation;
	}

	/**
	 * Create a {@link RepresentationInfo}.
	 * 
	 * @return A {@link RepresentationInfo}
	 */
	public RepresentationInfo getInfo()
	{
		return new RepresentationInfo( mediaType, modificationDate );
	}

	//
	// Externalizable
	//

	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
	{
		int byteSize = in.readInt();
		bytes = new byte[byteSize];
		in.readFully( bytes );
		string = in.readUTF();
		mediaType = MediaType.valueOf( in.readUTF() );
		language = Language.valueOf( in.readUTF() );
		characterSet = CharacterSet.valueOf( in.readUTF() );
		encoding = Encoding.valueOf( in.readUTF() );
		documentModificationDate = new Date( in.readLong() );
		modificationDate = new Date( in.readLong() );
		expirationDate = new Date( in.readLong() );
	}

	public void writeExternal( ObjectOutput out ) throws IOException
	{
		out.writeInt( bytes.length );
		out.write( bytes );
		out.writeUTF( nonNull( string ) );
		out.writeUTF( nonNull( mediaType ) );
		out.writeUTF( nonNull( language ) );
		out.writeUTF( nonNull( characterSet ) );
		out.writeUTF( nonNull( encoding ) );
		out.writeLong( documentModificationDate.getTime() );
		out.writeLong( modificationDate.getTime() );
		out.writeLong( expirationDate.getTime() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The stored bytes.
	 */
	private byte[] bytes;

	/**
	 * The stored string.
	 */
	private String string;

	/**
	 * The media type.
	 */
	private MediaType mediaType;

	/**
	 * The language.
	 */
	private Language language;

	/**
	 * The character set.
	 */
	private CharacterSet characterSet;

	/**
	 * The encoding.
	 */
	private Encoding encoding;

	/**
	 * The document modification date.
	 */
	private Date documentModificationDate;

	/**
	 * The entry modification date.
	 */
	private Date modificationDate = new Date();

	/**
	 * The expiration date.
	 */
	private Date expirationDate;

	/**
	 * Makes sure to return a non-null string.
	 * 
	 * @param metadata
	 *        The metadata or null
	 * @return A string
	 */
	private static String nonNull( Metadata metadata )
	{
		return metadata == null ? "" : nonNull( metadata.getName() );
	}

	/**
	 * Makes sure to return a non-null string.
	 * 
	 * @param string
	 *        The string or null
	 * @return A string
	 */
	private static String nonNull( String string )
	{
		return string == null ? "" : string;
	}
}