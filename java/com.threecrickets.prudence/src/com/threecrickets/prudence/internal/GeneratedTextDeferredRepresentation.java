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
import java.io.Writer;
import java.util.Arrays;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.WriterRepresentation;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.service.GeneratedTextResourceConversationService;
import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;
import com.threecrickets.scripturian.Executable;
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
	 * @param resource
	 *        The resource
	 * @param executable
	 *        The executable
	 * @param executionContext
	 *        The execution context
	 * @param documentService
	 *        The document service
	 * @param conversationService
	 *        The conversation service
	 */
	public GeneratedTextDeferredRepresentation( GeneratedTextResource resource, Executable executable, ExecutionContext executionContext, GeneratedTextResourceDocumentService documentService,
		GeneratedTextResourceConversationService conversationService )
	{
		this( resource, executable, executionContext, documentService, conversationService, 0 );
	}

	/**
	 * Construction.
	 * 
	 * @param resource
	 *        The resource
	 * @param executable
	 *        The executable
	 * @param executionContext
	 *        The execution context
	 * @param documentService
	 *        The document service
	 * @param conversationService
	 *        The conversation service
	 * @param delay
	 *        Delay in millseconds before committing response (for concurrency
	 *        testing purposes)
	 */
	public GeneratedTextDeferredRepresentation( GeneratedTextResource resource, Executable executable, ExecutionContext executionContext, GeneratedTextResourceDocumentService documentService,
		GeneratedTextResourceConversationService conversationService, long delay )
	{
		// Note that we are setting representation characteristics
		// before we actually execute the executable
		super( conversationService.getMediaType() );

		this.resource = resource;
		this.executable = executable;
		this.executionContext = executionContext;
		this.documentService = documentService;
		this.delay = delay;

		setCharacterSet( conversationService.getCharacterSet() );
		if( conversationService.getLanguage() != null )
			setLanguages( Arrays.asList( conversationService.getLanguage() ) );
		if( conversationService.getEncoding() != null )
			setEncodings( Arrays.asList( conversationService.getEncoding() ) );
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		executionContext.setWriter( writer );
		try
		{
			executable.execute( executionContext, documentService, resource.getExecutionController() );
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
			executionContext.getErrorWriterOrDefault().flush();
			resource.commit();
		}
	}

	@Override
	public void release()
	{
		executionContext.release();
		super.release();
		if( !resource.isCommitted() )
			resource.abort();
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

		Response response = resource.getResponse();
		response.setEntity( this );
		response.setStatus( Status.SUCCESS_OK );
		response.commit();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final GeneratedTextResource resource;

	/**
	 * The executable.
	 */
	private final Executable executable;

	/**
	 * The execution context.
	 */
	private final ExecutionContext executionContext;

	/**
	 * The document service.
	 */
	private final GeneratedTextResourceDocumentService documentService;

	/**
	 * Delay in millseconds before committing response. (For concurrency testing
	 * purposes.)
	 */
	private final long delay;
}