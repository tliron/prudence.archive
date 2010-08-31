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

import java.util.Date;
import java.util.Iterator;

import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.DelegatedResource;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class DelegatedResourceConversationService extends ResourceConversationServiceBase<DelegatedResource>
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
	public DelegatedResourceConversationService( DelegatedResource resource, Representation entity, Variant variant, CharacterSet defaultCharacterSet )
	{
		super( resource, entity, variant, defaultCharacterSet, resource.getFileUploadSizeThreshold(), resource.getFileUploadDirectory() );
	}

	/**
	 * The expiration date.
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
	 * The modification date.
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
	 * The tag.
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
	 * @return The max age in seconds, or -1 if not set
	 * @see #setMaxAge(int)
	 */
	public int getMaxAge()
	{
		for( CacheDirective cacheDirective : getResource().getResponse().getCacheDirectives() )
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
		for( Iterator<CacheDirective> i = getResource().getResponse().getCacheDirectives().iterator(); i.hasNext(); )
			if( i.next().getName().equals( HeaderConstants.CACHE_MAX_AGE ) )
				i.remove();

		if( maxAge != -1 )
			getResource().getResponse().getCacheDirectives().add( CacheDirective.maxAge( maxAge ) );
		else
			getResource().getResponse().getCacheDirectives().add( CacheDirective.noCache() );
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
		getResource().getVariants().add( new Variant( mediaType ) );
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
		getResource().getVariants().add( new Variant( MediaType.valueOf( mediaTypeName ) ) );
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
		getResource().getVariants().add( new Variant( getResource().getApplication().getMetadataService().getMediaType( mediaTypeExtension ) ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The expiration date.
	 */
	private Date expirationDate;

	/**
	 * The modification date.
	 */
	private Date modificationDate;

	/**
	 * The tag.
	 */
	private Tag tag;
}
