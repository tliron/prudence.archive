/**
 * Copyright 2009-2011 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.io.File;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;

/**
 * A parameter for files.
 * 
 * @author Tal Liron
 * @see FormWithFiles
 */
public class FileParameter extends Parameter
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        The parameter name
	 * @param data
	 *        The data
	 * @param mediaTypeName
	 *        The file media type name
	 * @param size
	 *        The file size
	 */
	public FileParameter( String name, byte[] data, String mediaTypeName, long size )
	{
		super( name, null );
		this.data = data;
		file = null;
		this.mediaTypeName = mediaTypeName;
		this.size = size;
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        The parameter name
	 * @param file
	 *        The file
	 * @param mediaTypeName
	 *        The file media type name
	 * @param size
	 *        The file size
	 */
	public FileParameter( String name, File file, String mediaTypeName, long size )
	{
		super( name, null );
		data = null;
		this.file = file;
		this.mediaTypeName = mediaTypeName;
		this.size = size;
	}

	//
	// Attributes
	//

	/**
	 * The data. If this is null, use {@link #getFile()} instead.
	 * 
	 * @return The data or null
	 */
	public byte[] getData()
	{
		return data;
	}

	/**
	 * The file. If this is null, use {@link #getData()} instead.
	 * 
	 * @return The file or null
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * The file media type name.
	 * 
	 * @return The file media type name
	 * @see #getMediaType()
	 */
	public String getMediaTypeName()
	{
		return mediaTypeName;
	}

	/**
	 * The file media type.
	 * 
	 * @return The file media type
	 * @see #getMediaTypeName()
	 */
	public MediaType getMediaType()
	{
		return MediaType.valueOf( mediaTypeName );
	}

	/**
	 * The file size.
	 * 
	 * @return The file size
	 */
	public long getSize()
	{
		return size;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The data.
	 */
	private final byte[] data;

	/**
	 * The file.
	 */
	private final File file;

	/**
	 * The media type name.
	 */
	private final String mediaTypeName;

	/**
	 * The file size.
	 */
	private final long size;
}
