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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Vector;

/**
 * Utilities to unify and/or compress JavaScript source files, saving them as a
 * single file. Unifying them allows clients to retrieve the JavaScript via one
 * request rather than many. Compressing them makes them retrievable faster.
 * 
 * @author Tal Liron
 * @see JSMin
 */
public abstract class JavaScriptUnifyMinify
{
	//
	// Constants
	//

	public static final String EXTENSION = ".js";

	public static final String EXTENSION_MIN = ".min";

	public static final String ALL = "all" + EXTENSION;

	public static final String ALL_MIN = "all" + EXTENSION_MIN + EXTENSION;

	//
	// Static operations
	//

	/**
	 * Unifies all JavaScript files in the directory to "all.js", or unifies and
	 * minifies them to "all.min.js".
	 */
	public static void unify( File file, boolean minify ) throws IOException
	{
		if( !file.isDirectory() )
			throw new IOException();

		File newFile;
		InputStream in;
		OutputStream out;
		long newLastModified = 0;

		newFile = new File( file, minify ? ALL_MIN : ALL );

		String[] names = file.list( jsFilter );

		for( String name : names )
		{
			long lastModified = new File( file, name ).lastModified();
			if( lastModified > newLastModified )
				newLastModified = lastModified;
		}

		if( newFile.lastModified() == newLastModified )
			return;

		Arrays.sort( names );
		Vector<InputStream> ins = new Vector<InputStream>();
		for( String name : names )
			ins.add( new FileInputStream( new File( file, name ) ) );

		in = new SequenceInputStream( ins.elements() );

		if( newFile.exists() )
			if( !newFile.delete() )
				throw new IOException( "Could not delete file: " + newFile );

		out = new FileOutputStream( newFile );
		if( minify )
			minify( in, out );
		else
			copy( in, out );
		in.close();
		out.close();

		if( !newFile.setLastModified( newLastModified ) )
			throw new IOException( "Could not update timestamp on file: " + newFile );
	}

	/**
	 * Minifies a JavaScript file into a new file with the extension of
	 * "min.js".
	 */
	public static void minify( File file ) throws IOException
	{
		if( file.isDirectory() )
			throw new IOException();

		File newFile;
		InputStream in;
		OutputStream out;
		long newLastModified = 0;

		String newPath = file.getPath();
		if( newPath.endsWith( EXTENSION ) )
			newPath = newPath.substring( 0, newPath.length() - EXTENSION.length() );
		newPath += EXTENSION_MIN + EXTENSION;
		newFile = new File( newPath );
		newLastModified = newFile.lastModified();

		if( newLastModified == file.lastModified() )
			return;

		in = new FileInputStream( file );

		if( newFile.exists() )
			if( !newFile.delete() )
				throw new IOException( "Could not delete file: " + newFile );

		out = new FileOutputStream( newFile );
		minify( in, out );
		in.close();
		out.close();
		if( !newFile.setLastModified( newLastModified ) )
			throw new IOException( "Could not update timestamp on file: " + newFile );
	}

	public static void minify( InputStream in, OutputStream out ) throws IOException
	{
		JSMin jsMin = new JSMin( in, out );
		try
		{
			jsMin.jsmin();
		}
		catch( Exception x )
		{
			IOException iox = new IOException();
			iox.initCause( x );
			throw iox;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Private constructor to avoid inheritence.
	 */
	private JavaScriptUnifyMinify()
	{
	}

	/**
	 * Filename filter for JavaScript files.
	 */
	private static final JSFilter jsFilter = new JSFilter();

	/**
	 * Filename filter for JavaScript files.
	 */
	private static class JSFilter implements FilenameFilter
	{
		public boolean accept( File directory, String name )
		{
			return name.endsWith( EXTENSION ) && !name.equals( ALL_MIN ) && !name.equals( ALL );
		}
	}

	/**
	 * Copies streams.
	 * 
	 * @param in
	 *        In stream
	 * @param out
	 *        Out stream
	 * @throws IOException
	 */
	private static void copy( InputStream in, OutputStream out ) throws IOException
	{
		while( true )
		{
			int data = in.read();
			if( data == -1 )
				break;
			out.write( data );
		}
	}
}