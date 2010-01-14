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

package com.threecrickets.prudence;

import org.restlet.Application;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Resource access utility methods for Prudence.
 * 
 * @author Tal Liron
 */
public abstract class Resource
{
	public static Representation get( String resourceUri, String mediaType )
	{
		ClientResource clientResource = new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_APPLICATION, resourceUri ) );
		if( mediaType == null )
			return clientResource.get();
		else
			return clientResource.get( getMediaType( mediaType ) );
	}

	public static Representation get( String applicationInternalName, String resourceUri, String mediaType )
	{
		ClientResource clientResource = new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, "/" + applicationInternalName + "/" + resourceUri ) );
		if( mediaType == null )
			return clientResource.get();
		else
			return clientResource.get( getMediaType( mediaType ) );
	}

	public static Representation getExternal( String uri, String mediaType )
	{
		ClientResource clientResource = new ClientResource( uri );
		if( mediaType == null )
			return clientResource.get();
		else
			return clientResource.get( getMediaType( mediaType ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Private constructor.
	 */
	private Resource()
	{
	}

	private static MediaType getMediaType( String name )
	{
		MediaType mediaType = MediaType.valueOf( name );
		if( mediaType == null )
			mediaType = Application.getCurrent().getMetadataService().getMediaType( name );
		return mediaType;
	}
}
