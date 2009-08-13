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

import javax.script.ScriptException;

import org.restlet.data.Language;

import com.threecrickets.prudence.ScriptedTextResource;

import org.restlet.representation.WriterRepresentation;

import com.threecrickets.scripturian.CompositeScript;
import com.threecrickets.scripturian.CompositeScriptContext;
import com.threecrickets.scripturian.ScriptContextController;

/**
 * Representation used in streaming mode of {@link ScriptedTextResource}.
 * 
 * @author Tal Liron
 * @ScriptedTextResource
 */
class ScriptedTextStreamingRepresentation extends WriterRepresentation
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
	 * @param compositeScriptContext
	 *        The composite script context
	 * @param scriptContextController
	 *        The script context controller
	 * @param script
	 *        The composite script instance
	 * @param flushLines
	 *        Whether to flush the writers after every line
	 */
	public ScriptedTextStreamingRepresentation( ScriptedTextResource resource, ExposedScriptedTextResourceContainer container, CompositeScriptContext compositeScriptContext,
		ScriptContextController scriptContextController, CompositeScript script, boolean flushLines )
	{
		// Note that we are setting representation characteristics
		// before we actually run the script
		super( container.getMediaType() );

		this.resource = resource;
		this.container = container;
		this.compositeScriptContext = compositeScriptContext;
		this.scriptContextController = scriptContextController;
		this.flushLines = flushLines;

		setCharacterSet( container.getCharacterSet() );
		if( container.getLanguage() != null )
		{
			setLanguages( Arrays.asList( new Language[]
			{
				container.getLanguage()
			} ) );
		}

		this.script = script;
	}

	//
	// WriterRepresentation
	//

	@Override
	public void write( Writer writer ) throws IOException
	{
		// writer = new OutputStreamWriter(System.out);
		this.container.isStreaming = true;
		this.resource.setWriter( writer );
		try
		{
			this.script.run( false, writer, this.resource.getErrorWriter(), this.flushLines, this.compositeScriptContext, this.container, this.scriptContextController );
		}
		catch( ScriptException x )
		{
			IOException iox = new IOException( "Script exception" );
			iox.initCause( x );
			throw iox;
		}
		finally
		{
			// The script may have set its cacheDuration, so we must
			// make sure to disable it!
			this.script.setCacheDuration( 0 );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final ScriptedTextResource resource;

	/**
	 * The container.
	 */
	private final ExposedScriptedTextResourceContainer container;

	/**
	 * The composite script instance.
	 */
	private final CompositeScript script;

	/**
	 * The script context controller.
	 */
	private final ScriptContextController scriptContextController;

	/**
	 * The composite script context.
	 */
	private final CompositeScriptContext compositeScriptContext;

	/**
	 * Whether to flush the writers after every line.
	 */
	private final boolean flushLines;
}