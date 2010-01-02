/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.restlet.data.Language;
import org.restlet.representation.WriterRepresentation;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentContext;
import com.threecrickets.scripturian.ScriptletController;
import com.threecrickets.scripturian.exception.DocumentInitializationException;
import com.threecrickets.scripturian.exception.DocumentRunException;

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
	 * @param resource
	 *        The resource
	 * @param container
	 *        The container
	 * @param documentContext
	 *        The document context
	 * @param scriptletController
	 *        The scriptlet controller
	 * @param document
	 *        The document instance
	 * @param flushLines
	 *        Whether to flush the writers after every line
	 */
	public GeneratedTextStreamingRepresentation( GeneratedTextResource resource, ExposedContainerForGeneratedTextResource container, DocumentContext documentContext, ScriptletController scriptletController,
		Document document, boolean flushLines )
	{
		// Note that we are setting representation characteristics
		// before we actually run the document
		super( container.getMediaType() );

		this.resource = resource;
		this.container = container;
		this.documentContext = documentContext;
		this.scriptletController = scriptletController;
		this.flushLines = flushLines;

		setCharacterSet( container.getCharacterSet() );
		if( container.getLanguage() != null )
		{
			setLanguages( Arrays.asList( new Language[]
			{
				container.getLanguage()
			} ) );
		}

		this.document = document;
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		container.isStreaming = true;
		resource.setWriter( writer );
		try
		{
			document.run( false, writer, resource.getErrorWriter(), flushLines, documentContext, container, scriptletController );
		}
		catch( DocumentInitializationException x )
		{
			IOException iox = new IOException( "DocumentParseException" );
			iox.initCause( x );
			throw iox;
		}
		catch( DocumentRunException x )
		{
			IOException iox = new IOException( "DocumentRunException" );
			iox.initCause( x );
			throw iox;
		}
		finally
		{
			// Scriptlets may have set its cacheDuration, so we must
			// make sure to disable it!
			document.setCacheDuration( 0 );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final GeneratedTextResource resource;

	/**
	 * The container.
	 */
	private final ExposedContainerForGeneratedTextResource container;

	/**
	 * The document instance.
	 */
	private final Document document;

	/**
	 * The scriptlet controller.
	 */
	private final ScriptletController scriptletController;

	/**
	 * The document context.
	 */
	private final DocumentContext documentContext;

	/**
	 * Whether to flush the writers after every line.
	 */
	private final boolean flushLines;
}