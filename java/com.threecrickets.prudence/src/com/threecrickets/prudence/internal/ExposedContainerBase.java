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
import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.util.CaptiveRedirector;
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

	/**
	 * Construction.
	 * 
	 * @param resource
	 *        The resource
	 * @param documentSource
	 *        The document source
	 */
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
	 * Access a resource internal to the current application.
	 * 
	 * @param resourceUri
	 *        The URI
	 * @param mediaType
	 *        The default media type of requests
	 * @return The client proxy
	 */
	public ClientResource internal( String resourceUri, String mediaType )
	{
		return new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_APPLICATION, resourceUri ) );
	}

	/**
	 * Access a resource internal to the current Prudence instance.
	 * 
	 * @param applicationInternalName
	 *        The application internal name
	 * @param resourceUri
	 *        The URI
	 * @param mediaType
	 *        The default media type of requests
	 * @return The client proxy
	 */
	public ClientResource internal( String applicationInternalName, String resourceUri, String mediaType )
	{
		return new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, "/" + applicationInternalName + "/" + resourceUri ) );
	}

	/**
	 * Access any resource.
	 * 
	 * @param uri
	 *        The URI
	 * @param mediaType
	 *        The default media type of requests
	 * @return The client proxy
	 */
	public ClientResource external( String uri, String mediaType )
	{
		return new ClientResource( uri );
	}

	/**
	 * The relative path that would reach the base URI of the application if
	 * appended to the current resource URI.
	 * 
	 * @return The relative path
	 */
	public String getPathToBase()
	{
		Request request = Request.getCurrent();
		Reference reference = CaptiveRedirector.getCaptiveReference( request );
		if( reference == null )
			reference = request.getResourceRef();

		String relative = reference.getRelativeRef().toString();

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
	 * Get a media type by its MIME type name.
	 * 
	 * @param name
	 *        The MIME type name
	 * @return The media type
	 */
	public MediaType getMediaType( String name )
	{
		MediaType mediaType = MediaType.valueOf( name );
		if( mediaType == null )
			mediaType = Application.getCurrent().getMetadataService().getMediaType( name );
		return mediaType;
	}

	/**
	 * A map of all values global to the current applications.
	 * 
	 * @return The globals
	 */
	public ConcurrentMap<String, Object> getGlobals()
	{
		return Application.getCurrent().getContext().getAttributes();
	}

	/**
	 * Gets a value global to the current application.
	 * 
	 * @param name
	 *        The name of the global
	 * @return The global's current value
	 */
	public Object getGlobal( String name )
	{
		return getGlobal( name, null );
	}

	/**
	 * Gets a value global to the current application, atomically setting it to
	 * a default value if it doesn't exist.
	 * 
	 * @param name
	 *        The name of the global
	 * @param defaultValue
	 *        The default value
	 * @return The global's current value
	 */
	public Object getGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> attributes = Application.getCurrent().getContext().getAttributes();
		Object value = attributes.get( name );
		if( ( value == null ) && ( defaultValue != null ) )
		{
			value = defaultValue;
			Object existing = attributes.putIfAbsent( name, value );
			if( existing != null )
				value = existing;
		}
		return value;
	}

	/**
	 * Sets the value global to the current application.
	 * 
	 * @param name
	 *        The name of the global
	 * @param value
	 *        The global's new value
	 * @return The global's previous value
	 */
	public Object setGlobal( String name, Object value )
	{
		ConcurrentMap<String, Object> attributes = Application.getCurrent().getContext().getAttributes();
		return attributes.put( name, value );
	}

	//
	// Operations
	//

	/**
	 * @param documentName
	 * @return
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	public abstract Representation include( String documentName ) throws IOException, ParsingException, ExecutionException;

	/**
	 * Throws a runtime exception.
	 * 
	 * @return Always throws an exception, so nothing is ever returned
	 */
	public boolean kaboom()
	{
		throw new RuntimeException( "Kaboom!" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The resource.
	 */
	protected final R resource;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document source.
	 */
	private final DocumentSource<Executable> documentSource;
}
