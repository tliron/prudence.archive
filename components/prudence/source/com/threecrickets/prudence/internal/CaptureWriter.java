/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;

/**
 * A {@link PrintWriter} wrapped around a {@link StringWriter}.
 * 
 * @author Tal Liron
 * @see GeneratedTextResourceDocumentService#startCapture(String)
 **/
public class CaptureWriter extends PrintWriter
{
	//
	// Construction
	//

	public CaptureWriter( String name )
	{
		super( new StringWriter(), false );
		this.name = name;
	}

	//
	// Attributes
	//

	public final String name;

	//
	// Object
	//

	@Override
	public String toString()
	{
		return ( (StringWriter) out ).toString();
	}
}