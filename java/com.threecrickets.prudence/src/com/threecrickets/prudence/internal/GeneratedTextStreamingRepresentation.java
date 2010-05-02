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
import com.threecrickets.scripturian.ExecutionContext;
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
	 * Construction.
	 * 
	 * @param exposedContainer
	 *        The exposed container to clone
	 */
	public GeneratedTextStreamingRepresentation( ExposedContainerForGeneratedTextResource exposedContainer )
	{
		// Note that we are setting representation characteristics
		// before we actually execute the executable
		super( exposedContainer.exposedConversation.getMediaType() );

		// Clone execution context
		ExecutionContext executionContext = new ExecutionContext();

		// Clone container
		this.exposedContainer = new ExposedContainerForGeneratedTextResource( exposedContainer.resource, executionContext, exposedContainer.exposedConversation.getEntity(), exposedContainer.exposedConversation
			.getVariant() );
		this.exposedContainer.executable = exposedContainer.executable;
		this.exposedContainer.exposedConversation.isStreaming = true;

		// Initialize execution context
		executionContext.getExposedVariables().put( this.exposedContainer.resource.getContainerName(), this.exposedContainer );
		executionContext.getExposedVariables().put( this.exposedContainer.resource.getConversationName(), this.exposedContainer.exposedConversation );

		setCharacterSet( this.exposedContainer.exposedConversation.getCharacterSet() );
		if( this.exposedContainer.exposedConversation.getLanguage() != null )
			setLanguages( Arrays.asList( this.exposedContainer.exposedConversation.getLanguage() ) );
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		exposedContainer.writer = writer;
		exposedContainer.executionContext.setWriter( writer );
		try
		{
			exposedContainer.executable.execute( exposedContainer.executionContext, exposedContainer, exposedContainer.resource.getExecutionController() );
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
			exposedContainer.executionContext.getErrorWriterOrDefault().flush();
		}
	}

	@Override
	public void release()
	{
		exposedContainer.executionContext.release();
		super.release();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The exposed container.
	 */
	private final ExposedContainerForGeneratedTextResource exposedContainer;
}