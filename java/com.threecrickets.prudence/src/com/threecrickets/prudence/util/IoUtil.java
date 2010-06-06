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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility methods for I/O and files.
 * 
 * @author Tal Liron
 */
public abstract class IoUtil
{
	/**
	 * Copies streams. The input stream is entirely consumed and closed.
	 * 
	 * @param in
	 *        Input stream
	 * @param out
	 *        Output stream
	 * @throws IOException
	 */
	public static void copyStream( InputStream in, OutputStream out ) throws IOException
	{
		in = new BufferedInputStream( in );
		try
		{
			out = new BufferedOutputStream( out );
			try
			{
				while( true )
				{
					int data = in.read();
					if( data == -1 )
						break;
					out.write( data );
				}
			}
			finally
			{
				out.flush();
			}
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * Loads properties from a file if it exists.
	 * 
	 * @param file
	 *        The file
	 * @return The properties
	 * @throws IOException
	 */
	public static Properties loadProperties( File file ) throws IOException
	{
		Properties properties = new Properties();
		try
		{
			FileInputStream in = new FileInputStream( file );
			try
			{
				properties.load( in );
			}
			finally
			{
				in.close();
			}
		}
		catch( FileNotFoundException x )
		{
			// This is allowed.
		}
		return properties;
	}

	/**
	 * Saves properties to a file.
	 * 
	 * @param properties
	 *        The properties
	 * @param file
	 *        The file
	 * @throws IOException
	 */
	public static void saveProperties( Properties properties, File file ) throws IOException
	{
		FileOutputStream out = new FileOutputStream( file );
		try
		{
			properties.store( out, "Modifications you make to this file may be overriden by Prudence!" );
		}
		finally
		{
			out.close();
		}
	}

	/**
	 * Makes sure a file instance is unique to this VM for its absolute path.
	 * Useful for synchronizing on a file.
	 * 
	 * @param file
	 *        The file instance
	 * @return The unique file instance
	 */
	public static File getUniqueFile( File file )
	{
		String key = file.getAbsolutePath();
		File uniqueFile = uniqueFiles.get( key );
		if( uniqueFile == null )
		{
			uniqueFile = file;
			File existing = uniqueFiles.putIfAbsent( key, uniqueFile );
			if( existing != null )
				uniqueFile = existing;
		}
		return uniqueFile;
	}

	/**
	 * Recursively packs a directory into a zip file.
	 * 
	 * @param directory
	 *        The directory to zip
	 * @param file
	 *        The zip file
	 * @throws IOException
	 */
	public static final void zip( File directory, File file ) throws IOException
	{
		ZipOutputStream out = new ZipOutputStream( new FileOutputStream( file ) );
		try
		{
			zip( directory, directory.getPath().length() + 1, out );
		}
		finally
		{
			out.close();
		}
	}

	/**
	 * Unpacks a zip file into a directory.
	 * 
	 * @param file
	 *        The zip file
	 * @param directory
	 *        The directory
	 * @throws IOException
	 */
	public static void unzip( File file, File directory ) throws IOException
	{
		ZipFile zipFile = new ZipFile( file );
		try
		{
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while( entries.hasMoreElements() )
			{
				ZipEntry entry = entries.nextElement();
				File entryFile = new File( directory, entry.getName() );

				if( entry.isDirectory() )
					entryFile.mkdirs();
				else
				{
					InputStream in = zipFile.getInputStream( entry );
					try
					{
						OutputStream out = new FileOutputStream( entryFile );
						try
						{
							copyStream( in, out );
						}
						finally
						{
							out.close();
						}
					}
					finally
					{
						in.close();
					}
				}
			}
		}
		finally
		{
			zipFile.close();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private IoUtil()
	{
	}

	/**
	 * Cache of unique files.
	 */
	private static final ConcurrentMap<String, File> uniqueFiles = new ConcurrentHashMap<String, File>();

	/**
	 * Zip a directory.
	 * 
	 * @param directory
	 *        The directory
	 * @param pathPrefixLength
	 *        Number of characters to strip from beginning of entry path
	 * @param out
	 *        Output stream
	 * @throws IOException
	 */
	private static void zip( File directory, int pathPrefixLength, ZipOutputStream out ) throws IOException
	{
		File[] files = directory.listFiles();
		for( File file : files )
		{
			if( file.isDirectory() )
				zip( file, pathPrefixLength, out );
			else
			{
				ZipEntry entry = new ZipEntry( file.getPath().substring( pathPrefixLength ) );
				out.putNextEntry( entry );

				FileInputStream in = new FileInputStream( file );
				try
				{
					copyStream( in, out );
				}
				finally
				{
					in.close();
				}
			}
		}
	}
}
