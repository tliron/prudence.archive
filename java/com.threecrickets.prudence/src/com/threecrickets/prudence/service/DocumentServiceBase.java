/**
 * Copyright 2009-2011 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.restlet.Application;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 */
public abstract class DocumentServiceBase
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param documentSource
	 *        The document source
	 */
	public DocumentServiceBase( DocumentSource<Executable> documentSource )
	{
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

	//
	// Operations
	//

	/**
	 * Executes a source code document. The language of the source code will be
	 * determined by the document tag, which is usually the filename extension.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public Representation execute( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		DocumentDescriptor<Executable> documentDescriptor = getDocumentDescriptor( documentName );

		// Add dependency
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		if( currentDocumentDescriptor != null )
			currentDocumentDescriptor.getDependencies().add( documentDescriptor );

		// Execute
		pushDocumentDescriptor( documentDescriptor );
		try
		{
			documentDescriptor.getDocument().execute();
		}
		finally
		{
			popDocumentDescriptor();
		}

		return null;
	}

	/**
	 * Explicitly add a dependency to this document.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws DocumentException
	 * @throws ParsingException
	 */
	public void addDependency( String documentName ) throws ParsingException, DocumentException
	{
		// Add dependency
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		if( currentDocumentDescriptor != null )
			currentDocumentDescriptor.getDependencies().add( getDocumentDescriptor( documentName ) );
	}

	/**
	 * Explicitly add a dependency to this document. Unlike
	 * {@link #addDependency(String)}, any file can be added here. The contents
	 * of the file are never read.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws ParsingException
	 * @throws DocumentException
	 */
	public void addFileDependency( String documentName ) throws ParsingException, DocumentException
	{
		// Add dependency
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		if( currentDocumentDescriptor != null )
			currentDocumentDescriptor.getDependencies().add( getFileDocumentDescriptor( documentName ) );
	}

	/**
	 * Invalidates a document, which can affect documents that depend on it.
	 * 
	 * @throws DocumentException
	 * @throws ParsingException
	 * @throws IOException
	 */
	public void invalidate( String documentName ) throws ParsingException, DocumentException, IOException
	{
		DocumentDescriptor<Executable> documentDescriptor = getDocumentDescriptor( documentName );
		documentDescriptor.invalidate();
	}

	/**
	 * Invalidates the current document, which can affect documents that depend
	 * on it.
	 */
	public void invalidateCurrent()
	{
		DocumentDescriptor<Executable> currentDocumentDescriptor = getCurrentDocumentDescriptor();
		if( currentDocumentDescriptor != null )
			currentDocumentDescriptor.invalidate();
	}

	/**
	 * Access a resource internal to the current application.
	 * 
	 * @param resourceUri
	 *        The URI
	 * @param mediaTypeName
	 *        The preferred media type
	 * @return The client proxy
	 */
	public ClientResource internal( String resourceUri, String mediaTypeName )
	{
		ClientResource clientResource = new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_APPLICATION, resourceUri ) );
		clientResource.getClientInfo().getAcceptedMediaTypes().add( new Preference<MediaType>( getMediaType( mediaTypeName ), 1f ) );
		return clientResource;
	}

	/**
	 * Access a resource internal to the current Prudence instance.
	 * 
	 * @param applicationInternalName
	 *        The application internal name
	 * @param resourceUri
	 *        The URI
	 * @param mediaTypeName
	 *        The preferred media type
	 * @return The client proxy
	 */
	public ClientResource internal( String applicationInternalName, String resourceUri, String mediaTypeName )
	{
		ClientResource clientResource = new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, "/" + applicationInternalName + "/" + resourceUri ) );
		clientResource.getClientInfo().getAcceptedMediaTypes().add( new Preference<MediaType>( getMediaType( mediaTypeName ), 1f ) );
		return clientResource;
	}

	/**
	 * Access any resource.
	 * 
	 * @param uri
	 *        The URI
	 * @param mediaTypeName
	 *        The preferred media type
	 * @return The client proxy
	 */
	public ClientResource external( String uri, String mediaTypeName )
	{
		ClientResource clientResource = new ClientResource( uri );
		clientResource.getClientInfo().getAcceptedMediaTypes().add( new Preference<MediaType>( getMediaType( mediaTypeName ), 1f ) );
		return clientResource;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The document stack.
	 */
	protected LinkedList<DocumentDescriptor<Executable>> documentDescriptorStack = new LinkedList<DocumentDescriptor<Executable>>();

	/**
	 * Gets a document descriptor.
	 * 
	 * @param documentName
	 *        The document name
	 * @return The document descriptor
	 * @throws ParsingException
	 * @throws DocumentException
	 */
	protected abstract DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException;

	/**
	 * The currently executing document (the one at the top of the stack).
	 * 
	 * @return The current document or null
	 */
	protected DocumentDescriptor<Executable> getCurrentDocumentDescriptor()
	{
		try
		{
			return documentDescriptorStack.getLast();
		}
		catch( NoSuchElementException x )
		{
			return null;
		}
	}

	/**
	 * Add a document to the top of stack.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 */
	protected void pushDocumentDescriptor( DocumentDescriptor<Executable> documentDescriptor )
	{
		documentDescriptorStack.add( documentDescriptor );
	}

	/**
	 * Remove the top document from the stack.
	 */
	protected DocumentDescriptor<Executable> popDocumentDescriptor()
	{
		return documentDescriptorStack.removeLast();
	}

	/**
	 * If the {@link #getSource()} is a {@link DocumentFileSource}, then this is
	 * the file relative to the {@link DocumentFileSource#getBasePath()} .
	 * Otherwise, it's null.
	 * 
	 * @return The relative library directory or null
	 */
	protected File getRelativeFile( File file )
	{
		if( file != null )
		{
			if( documentSource instanceof DocumentFileSource<?> )
				return ScripturianUtil.getRelativeFile( file, ( (DocumentFileSource<?>) documentSource ).getBasePath() );
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document source.
	 */
	private final DocumentSource<Executable> documentSource;

	/**
	 * Get a media type by its MIME type name.
	 * 
	 * @param name
	 *        The MIME type name
	 * @return The media type
	 */
	private static MediaType getMediaType( String name )
	{
		MediaType mediaType = MediaType.valueOf( name );
		if( mediaType == null )
			mediaType = Application.getCurrent().getMetadataService().getMediaType( name );
		return mediaType;
	}

	/**
	 * Gets a document descriptor without reading the contents of the file. Only
	 * supported for {@link DocumentFileSource}.
	 * 
	 * @param documentName
	 *        The document name
	 * @return The document descriptor
	 * @throws ParsingException
	 * @throws DocumentException
	 */
	private DocumentDescriptor<Executable> getFileDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		if( documentSource instanceof DocumentFileSource<?> )
		{
			DocumentFileSource<Executable> documentFileSource = (DocumentFileSource<Executable>) documentSource;
			return documentFileSource.getDocument( documentName, false );
		}
		else
			throw new DocumentException( "File document descriptors only available for DocumentFileSource" );
	}

}
