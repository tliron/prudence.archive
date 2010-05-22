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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/**
 * A {@link Filter} that automatically unifies and/or minifies source files,
 * saving them as a single file. Unifying them allows clients to retrieve the
 * source via one request rather than many. Minifying them makes them
 * retrievable faster.
 * <p>
 * This filter can track changes to the source files, updating the result file
 * on-the-fly. This makes it easy to develop and debug a live site.
 * <p>
 * Note that this instances of this class can only guarantee atomic access to
 * the unified/minified version within the current VM.
 * 
 * @author Tal Liron
 */
public abstract class UnifyMinifyFilter extends Filter
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param sourceDirectory
	 *        The directory where the sources are found
	 * @param minimumTimeBetweenValidityChecks
	 *        See {@link #getMinimumTimeBetweenValidityChecks()}
	 * @param sourceExtension
	 *        The source extension
	 * @param minifiedSourceExtension
	 *        The minified source extension (comes before the source extension
	 *        for minified versions)
	 * @param unifiedFilename
	 *        The unified filename (the source extension is appended to it)
	 */
	public UnifyMinifyFilter( Context context, Restlet next, File sourceDirectory, long minimumTimeBetweenValidityChecks, String sourceExtension, String minifiedSourceExtension, String unifiedFilename )
	{
		super( context, next );
		this.sourceExtension = "." + sourceExtension;
		this.minifiedSourceExtension = "." + minifiedSourceExtension;
		this.unifiedFilename = unifiedFilename + this.sourceExtension;
		this.unifiedMinifiedFilename = unifiedFilename + this.minifiedSourceExtension + this.sourceExtension;
		this.sourceDirectory = sourceDirectory;
		this.minimumTimeBetweenValidityChecks.set( minimumTimeBetweenValidityChecks );
	}

	//
	// Attributes
	//

	/**
	 * A value of -1 disables all validity checking.
	 * 
	 * @return The minimum time between validity checks in milliseconds
	 * @see #setMinimumTimeBetweenValidityChecks(long)
	 */
	public long getMinimumTimeBetweenValidityChecks()
	{
		return minimumTimeBetweenValidityChecks.get();
	}

	/**
	 * @param minimumTimeBetweenValidityChecks
	 * @see #getMinimumTimeBetweenValidityChecks()
	 */
	public void setMinimumTimeBetweenValidityChecks( long minimumTimeBetweenValidityChecks )
	{
		this.minimumTimeBetweenValidityChecks.set( minimumTimeBetweenValidityChecks );
	}

	//
	// Operations
	//

	/**
	 * Unifies all source files in the directory, or unifies and minifies them.
	 */
	public void unify( File sourceDirectory, boolean minify ) throws IOException
	{
		File unifiedSourceFile = getUniqueFile( new File( sourceDirectory, minify ? unifiedMinifiedFilename : unifiedFilename ) );

		synchronized( unifiedSourceFile )
		{
			if( unifiedSourceFile.exists() )
				unifiedSourceFile.delete();

			String[] sourceFilenames = sourceDirectory.list( sourceFilenameFilter );

			long newLastModified = 0;

			for( String sourceFilename : sourceFilenames )
			{
				long lastModified = new File( sourceDirectory, sourceFilename ).lastModified();
				if( lastModified > newLastModified )
					newLastModified = lastModified;
			}

			if( unifiedSourceFile.lastModified() == newLastModified )
				return;

			Arrays.sort( sourceFilenames );
			Vector<InputStream> ins = new Vector<InputStream>();
			for( String name : sourceFilenames )
			{
				try
				{
					ins.add( new FileInputStream( new File( sourceDirectory, name ) ) );
				}
				catch( IOException x )
				{
					for( InputStream in : ins )
						in.close();
					throw x;
				}
			}

			InputStream in = new SequenceInputStream( ins.elements() );
			try
			{
				OutputStream out = new FileOutputStream( unifiedSourceFile );
				try
				{
					if( minify )
						minify( in, out );
					else
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

			if( !unifiedSourceFile.setLastModified( newLastModified ) )
				throw new IOException( "Could not update timestamp on file: " + unifiedSourceFile );
		}
	}

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		Reference reference = request.getResourceRef();
		String path = reference.getRemainingPart( true, false );
		try
		{
			boolean validate = false;
			boolean minify = false;
			if( path.endsWith( unifiedFilename ) )
				validate = true;
			else if( path.endsWith( unifiedMinifiedFilename ) )
			{
				validate = true;
				minify = true;
			}

			if( validate )
			{
				long now = System.currentTimeMillis();
				long lastValidityCheck = this.lastValidityCheck.get();
				if( lastValidityCheck == 0 || ( now - lastValidityCheck > minimumTimeBetweenValidityChecks.get() ) )
				{
					this.lastValidityCheck.set( now );

					unify( new File( sourceDirectory, path ).getParentFile(), minify );
				}
			}
		}
		catch( IOException x )
		{
			response.setStatus( Status.SERVER_ERROR_INTERNAL, x );
			return Filter.STOP;
		}

		return Filter.CONTINUE;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Minifies the stream.
	 * 
	 * @param in
	 *        Input stream
	 * @param out
	 *        Output stream
	 * @throws IOException
	 */
	protected abstract void minify( InputStream in, OutputStream out ) throws IOException;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The source extension.
	 */
	public final String sourceExtension;

	/**
	 * The minified source extension .
	 */
	public final String minifiedSourceExtension;

	/**
	 * The unified filename.
	 */
	public final String unifiedFilename;

	/**
	 * The unified-minified filename.
	 */
	public final String unifiedMinifiedFilename;

	/**
	 * The directory where the sources are found.
	 */
	private final File sourceDirectory;

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private final AtomicLong minimumTimeBetweenValidityChecks = new AtomicLong();

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private final AtomicLong lastValidityCheck = new AtomicLong();

	/**
	 * Filename filter for source files.
	 */
	private final SourceFilenameFilter sourceFilenameFilter = new SourceFilenameFilter();

	/**
	 * Filename filter for source files.
	 */
	private class SourceFilenameFilter implements FilenameFilter
	{
		public boolean accept( File directory, String name )
		{
			return name.endsWith( sourceExtension ) && !name.equals( unifiedMinifiedFilename ) && !name.equals( unifiedFilename );
		}
	}

	/**
	 * Cache of unique files.
	 */
	private static final ConcurrentMap<String, File> uniqueFiles = new ConcurrentHashMap<String, File>();

	/**
	 * Makes sure the file is unique.
	 * 
	 * @param file
	 *        The file
	 * @return The unique file
	 */
	private static File getUniqueFile( File file )
	{
		String key = file.getAbsolutePath();
		File uniqueFile = uniqueFiles.get( key );
		if( uniqueFile == null )
		{
			uniqueFile = file;
			File existing = uniqueFiles.put( key, uniqueFile );
			if( existing != null )
				uniqueFile = existing;
		}
		return uniqueFile;
	}

	/**
	 * Copies streams.
	 * 
	 * @param in
	 *        Input stream
	 * @param out
	 *        Output stream
	 * @throws IOException
	 */
	private static void copyStream( InputStream in, OutputStream out ) throws IOException
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
