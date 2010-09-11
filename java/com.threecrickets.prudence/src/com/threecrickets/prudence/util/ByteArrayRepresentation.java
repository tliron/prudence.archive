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

package com.threecrickets.prudence.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.restlet.data.MediaType;
import org.restlet.representation.CharacterRepresentation;
import org.restlet.representation.Representation;

/**
 * A {@link Representation} based on an array bytes.
 * 
 * @author Tal Liron
 */
public class ByteArrayRepresentation extends CharacterRepresentation
{
	//
	// Construction
	//

	public ByteArrayRepresentation( MediaType mediaType, byte[] bytes )
	{
		super( mediaType );
		this.bytes = bytes;
	}

	//
	// CharacterRepresentation
	//

	@Override
	public Reader getReader() throws IOException
	{
		return new StringReader( new String( bytes ) );
	}

	@Override
	public InputStream getStream() throws IOException
	{
		return new ByteArrayInputStream( bytes );
	}

	@Override
	public void write( Writer writer ) throws IOException
	{
		writer.write( new String( bytes ) );
	}

	@Override
	public void write( OutputStream outputStream ) throws IOException
	{
		IoUtil.copyStream( getStream(), outputStream );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final byte[] bytes;
}
