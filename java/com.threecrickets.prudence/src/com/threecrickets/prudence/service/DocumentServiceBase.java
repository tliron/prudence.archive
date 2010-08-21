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

import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.restlet.data.LocalReference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

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
	 */
	public void invalidate( String documentName ) throws ParsingException, DocumentException
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document source.
	 */
	private final DocumentSource<Executable> documentSource;

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
