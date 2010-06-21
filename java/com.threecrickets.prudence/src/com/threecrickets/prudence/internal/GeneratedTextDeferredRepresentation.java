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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.WriterRepresentation;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Representation used in deferred mode of {@link GeneratedTextResource}.
 * 
 * @author Tal Liron
 * @see GeneratedTextResource
 */
public class GeneratedTextDeferredRepresentation extends WriterRepresentation implements Runnable
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param exposedDocument
	 *        The exposed document to clone
	 */
	public GeneratedTextDeferredRepresentation( ExposedDocumentForGeneratedTextResource exposedDocument )
	{
		this( exposedDocument, 0 );
	}

	/**
	 * Construction.
	 * 
	 * @param exposedDocument
	 *        The exposed document to clone
	 * @param delay
	 *        Delay in millseconds before committing response (for concurrency
	 *        testing purposes)
	 */
	public GeneratedTextDeferredRepresentation( ExposedDocumentForGeneratedTextResource exposedDocument, long delay )
	{
		// Note that we are setting representation characteristics
		// before we actually execute the executable
		super( exposedDocument.exposedConversation.getMediaType() );

		this.delay = delay;

		// Clone execution context
		ExecutionContext executionContext = new ExecutionContext();

		// Clone container
		this.exposedDocument = new ExposedDocumentForGeneratedTextResource( exposedDocument.resource, executionContext, exposedDocument.exposedConversation.getEntity(), exposedDocument.exposedConversation.getVariant() );
		this.exposedDocument.currentExecutable = exposedDocument.currentExecutable;
		this.exposedDocument.exposedConversation.isDeferred = true;

		// Initialize execution context
		executionContext.getExposedVariables().put( this.exposedDocument.resource.getExposedDocumentName(), this.exposedDocument );
		executionContext.getExposedVariables().put( this.exposedDocument.resource.getExposedApplicationName(), this.exposedDocument.exposedApplication );
		executionContext.getExposedVariables().put( this.exposedDocument.resource.getExposedConversationName(), this.exposedDocument.exposedConversation );
		File libraryDirectory = this.exposedDocument.resource.getLibraryDirectory();
		if( libraryDirectory != null )
			executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

		setCharacterSet( this.exposedDocument.exposedConversation.getCharacterSet() );
		if( this.exposedDocument.exposedConversation.getLanguage() != null )
			setLanguages( Arrays.asList( this.exposedDocument.exposedConversation.getLanguage() ) );
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		exposedDocument.writer = writer;
		exposedDocument.executionContext.setWriter( writer );
		try
		{
			exposedDocument.currentExecutable.execute( exposedDocument.executionContext, exposedDocument, exposedDocument.resource.getExecutionController() );
		}
		catch( ParsingException x )
		{
			IOException iox = new IOException( "ParsingException" );
			iox.initCause( x );
			throw iox;
		}
		catch( ExecutionException x )
		{
			IOException iox = new IOException( "ExecutionException" );
			iox.initCause( x );
			throw iox;
		}
		finally
		{
			exposedDocument.executionContext.getErrorWriterOrDefault().flush();
			exposedDocument.resource.commit();
		}
	}

	@Override
	public void release()
	{
		exposedDocument.executionContext.release();
		super.release();
		if( !exposedDocument.resource.isCommitted() )
			exposedDocument.resource.getResponse().abort();
	}

	//
	// Runnable
	//

	public void run()
	{
		if( delay > 0 )
		{
			try
			{
				Thread.sleep( delay );
			}
			catch( InterruptedException e )
			{
				// Restore interrupt status
				Thread.currentThread().interrupt();
				return;
			}
		}

		Response response = exposedDocument.resource.getResponse();
		response.setEntity( this );
		response.setStatus( Status.SUCCESS_OK );
		response.commit();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The exposed container.
	 */
	private final ExposedDocumentForGeneratedTextResource exposedDocument;

	/**
	 * Delay in millseconds before committing response. (For concurrency testing
	 * purposes.)
	 */
	private final long delay;
}