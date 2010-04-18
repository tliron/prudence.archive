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

import org.restlet.data.CharacterSet;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.GeneratedTextResource;

/**
 * @author Tal Liron
 */
public class ExposedConversationForGeneratedTextResource extends ExposedConversationBase<GeneratedTextResource>
{
	//
	// Construction
	//

	public ExposedConversationForGeneratedTextResource( GeneratedTextResource resource, Representation entity, Variant variant, CharacterSet defaultCharacterSet )
	{
		super( resource, entity, variant, defaultCharacterSet );
	}

	//
	// Attributes
	//

	/**
	 * This boolean is true when the writer is in streaming mode.
	 * 
	 * @return True if in streaming mode, false if in caching mode
	 */
	public boolean isStreaming()
	{
		return isStreaming;
	}

	/**
	 * Identical to {@link #isStreaming()}. Supports scripting engines which
	 * don't know how to recognize the "is" getter notation, but can recognize
	 * the "get" notation.
	 * 
	 * @return True if in streaming mode, false if in caching mode
	 * @see #isStreaming()
	 */
	public boolean getIsStreaming()
	{
		return isStreaming();
	}

	//
	// Operations
	//

	/**
	 * If you are in caching mode, calling this method will return true and
	 * cause the document to run again, where this next run will be in streaming
	 * mode. Whatever output the document created in the current run is
	 * discarded, and all further exceptions are ignored. For this reason, it's
	 * probably best to call <code>prudence.stream()</code> as early as possible
	 * in the document, and then to quit the document as soon as possible if it
	 * returns true. For example, your document can start by testing whether it
	 * will have a lot of output, and if so, set output characteristics, call
	 * <code>prudence.stream()</code>, and quit. If you are already in streaming
	 * mode, calling this method has no effect and returns false. Note that a
	 * good way to quit the script is to throw an exception, because it will end
	 * the script and otherwise be ignored.
	 * <p>
	 * By default, writers will be automatically flushed after every line in
	 * streaming mode. If you want to disable this behavior, use
	 * {@link #stream(boolean)}.
	 * 
	 * @return True if started streaming mode, false if already in streaming
	 *         mode
	 * @see #stream(boolean)
	 */
	public boolean stream()
	{
		return stream( true );
	}

	/**
	 * This version of {@link #stream()} adds a boolean argument to let you
	 * control whether to flush the writer after every line in streaming mode.
	 * By default auto-flushing is enabled.
	 * 
	 * @param flushLines
	 *        Whether to flush the writers after every line in streaming mode
	 * @return True if started streaming mode, false if already in streaming
	 *         mode
	 * @see #stream()
	 */
	public boolean stream( boolean flushLines )
	{
		if( isStreaming )
			return false;

		startStreaming = true;
		this.flushLines = flushLines;
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Flag to signify that we should enter streaming mode.
	 */
	protected boolean startStreaming;

	/**
	 * This boolean is true when the writer is in streaming mode.
	 */
	protected boolean isStreaming;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Whether to flush the writers after every line in streaming mode.
	 */
	@SuppressWarnings("unused")
	private boolean flushLines;
}
