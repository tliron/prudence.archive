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
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.exception.ExecutionException;

/**
 * Representation used in streaming mode of {@link GeneratedTextResource}.
 * 
 * @author Tal Liron
 * @ScriptedTextResource
 */
class GeneratedTextStreamingRepresentation extends WriterRepresentation
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param container
	 *        The container
	 * @param executionContext
	 *        The document context
	 * @param executionController
	 *        The scriptlet controller
	 * @param document
	 *        The document instance
	 */
	public GeneratedTextStreamingRepresentation( ExposedContainerForGeneratedTextResource container, ExecutionContext executionContext, ExecutionController executionController, Executable document )
	{
		// Note that we are setting representation characteristics
		// before we actually run the document
		super( container.getMediaType() );

		this.container = container;
		this.executionContext = executionContext;
		this.executionController = executionController;

		setCharacterSet( container.getCharacterSet() );
		if( container.getLanguage() != null )
			setLanguages( Arrays.asList( container.getLanguage() ) );

		this.document = document;
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		container.isStreaming = true;
		executionContext.setWriter( writer );
		try
		{
			document.execute( false, executionContext, container, executionController );
		}
		catch( ParsingException x )
		{
			IOException iox = new IOException( "ExecutableInitializationException" );
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The container.
	 */
	private final ExposedContainerForGeneratedTextResource container;

	/**
	 * The document instance.
	 */
	private final Executable document;

	/**
	 * The scriptlet controller.
	 */
	private final ExecutionController executionController;

	/**
	 * The execution context.
	 */
	private final ExecutionContext executionContext;
}