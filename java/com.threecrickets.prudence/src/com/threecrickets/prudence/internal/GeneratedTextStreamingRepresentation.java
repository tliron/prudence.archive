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

import org.restlet.representation.WriterRepresentation;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Representation used in streaming mode of {@link GeneratedTextResource}.
 * 
 * @author Tal Liron
 * @see GeneratedTextResource
 */
public class GeneratedTextStreamingRepresentation extends WriterRepresentation
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param exposedConversation
	 *        The exposed container
	 * @param exposedConversation
	 *        The exposed conversation
	 * @param executionContext
	 *        The execution context (note that we are owning it and will release
	 *        it when done)
	 * @param executionController
	 *        The execution controller
	 * @param executable
	 *        The executable
	 */
	public GeneratedTextStreamingRepresentation( ExposedContainerForGeneratedTextResource exposedContainer, ExposedConversationForGeneratedTextResource exposedConversation, ExecutionContext executionContext,
		ExecutionController executionController, Executable executable )
	{
		// Note that we are setting representation characteristics
		// before we actually execute the executable
		super( exposedConversation.getMediaType() );

		this.exposedContainer = exposedContainer;
		this.exposedConversation = exposedConversation;
		this.executionContext = executionContext;
		this.executionController = executionController;
		this.executable = executable;

		setCharacterSet( exposedConversation.getCharacterSet() );
		if( exposedConversation.getLanguage() != null )
			setLanguages( Arrays.asList( exposedConversation.getLanguage() ) );
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		exposedConversation.isStreaming = true;
		exposedConversation.getResource().setWriter( writer );
		executionContext.setWriter( writer );
		try
		{
			executable.execute( executionContext, exposedContainer, executionController );
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
	}

	@Override
	public void release()
	{
		executionContext.release();
		super.release();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The exposed container.
	 */
	private final ExposedContainerForGeneratedTextResource exposedContainer;

	/**
	 * The exposed conversation.
	 */
	private final ExposedConversationForGeneratedTextResource exposedConversation;

	/**
	 * The executable.
	 */
	private final Executable executable;

	/**
	 * The execution controller.
	 */
	private final ExecutionController executionController;

	/**
	 * The execution context.
	 */
	private final ExecutionContext executionContext;
}