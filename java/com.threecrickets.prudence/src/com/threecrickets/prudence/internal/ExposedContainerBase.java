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

package com.threecrickets.prudence.internal;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ServerResource;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Resource access utility methods for Prudence.
 * 
 * @author Tal Liron
 */
public abstract class ExposedContainerBase<R extends ServerResource>
{
	//
	// Construction
	//

	public ExposedContainerBase( R resource, DocumentSource<Executable> documentSource )
	{
		this.resource = resource;
		this.documentSource = documentSource;
	}

	//
	// Attributes
	//

	/**
	 * The {@link DocumentSource} used to fetch executables.
	 * 
	 * @return The document source
	 */
	public DocumentSource<Executable> getSource()
	{
		return documentSource;
	}

	/**
	 * @param resourceUri
	 * @param mediaType
	 * @return
	 */
	public ClientResource internal( String resourceUri, String mediaType )
	{
		return new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_APPLICATION, resourceUri ) );
	}

	/**
	 * @param applicationInternalName
	 * @param resourceUri
	 * @param mediaType
	 * @return
	 */
	public ClientResource internal( String applicationInternalName, String resourceUri, String mediaType )
	{
		return new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, "/" + applicationInternalName + "/" + resourceUri ) );
	}

	/**
	 * @param uri
	 * @param mediaType
	 * @return
	 */
	public ClientResource external( String uri, String mediaType )
	{
		return new ClientResource( uri );
	}

	/**
	 * @return
	 */
	public String getPathToBase()
	{
		Request request = Request.getCurrent();
		String relative = request.getResourceRef().getRelativeRef().toString();

		// Remove redundant slashes
		relative = relative.replace( "//", "/" );

		// Count segments
		int segments = 0;
		for( int index = relative.indexOf( '/' ); index != -1; index = relative.indexOf( '/', index + 1 ) )
			segments++;

		// Build relative path
		StringBuilder path = new StringBuilder();
		for( int i = 0; i < segments; i++ )
			path.append( "../" );

		// System.out.println( relative + " - " + path );
		return path.toString();
	}

	/**
	 * @param name
	 * @return
	 */
	public MediaType getMediaType( String name )
	{
		MediaType mediaType = MediaType.valueOf( name );
		if( mediaType == null )
			mediaType = Application.getCurrent().getMetadataService().getMediaType( name );
		return mediaType;
	}

	//
	// Operations
	//

	public abstract Representation include( String documentName ) throws IOException, ParsingException, ExecutionException;

	/**
	 * Throws a runtime exception.
	 * 
	 * @return
	 */
	public boolean kaboom()
	{
		throw new RuntimeException( "Kaboom!" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final R resource;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final DocumentSource<Executable> documentSource;
}
