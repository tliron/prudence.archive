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
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.representation.StringRepresentation;

/**
 * A serializable, cacheable set of parameters from which
 * {@link StringRepresentation} instances can be created.
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
	 * Constructor. A constructor without arguments is requires for
	 * {@link Externalizable}.
	 */
	public CacheEntry()
	{
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
	 * @param documentModificationDate
	 *        The document modification date
	 * @param expirationDate
	 *        The expiration date
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, Date documentModificationDate, Date expirationDate )
	{
		this.string = string;
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
		this.documentModificationDate = documentModificationDate;
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
	 * @param docmuentModificationTimestamp
	 *        The document modification timestamp
	 * @param expirationTimestamp
	 *        The expiration timestamp or 0 for no expiration
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, long docmuentModificationTimestamp, long expirationTimestamp )
	{
		this( string, mediaType, language, characterSet, new Date( docmuentModificationTimestamp ), expirationTimestamp > 0 ? new Date( expirationTimestamp ) : null );
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

	//
	// Externalizable
	//

	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
	{
		string = in.readUTF();
		mediaType = MediaType.valueOf( in.readUTF() );
		language = Language.valueOf( in.readUTF() );
		characterSet = CharacterSet.valueOf( in.readUTF() );
		documentModificationDate = new Date( in.readLong() );
		modificationDate = new Date( in.readLong() );
		expirationDate = new Date( in.readLong() );
	}

	public void writeExternal( ObjectOutput out ) throws IOException
	{
		out.writeUTF( nonNull( string ) );
		out.writeUTF( nonNull( mediaType ) );
		out.writeUTF( nonNull( language ) );
		out.writeUTF( nonNull( characterSet ) );
		out.writeLong( documentModificationDate.getTime() );
		out.writeLong( modificationDate.getTime() );
		out.writeLong( expirationDate.getTime() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

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