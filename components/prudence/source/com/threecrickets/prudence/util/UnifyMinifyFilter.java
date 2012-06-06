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

package com.threecrickets.prudence.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

import com.hazelcast.util.ConcurrentHashSet;

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
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param targetDirectory
	 *        The directory into which unified-minified results should be
	 *        written
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
	public UnifyMinifyFilter( Context context, File targetDirectory, long minimumTimeBetweenValidityChecks, String sourceExtension, String minifiedSourceExtension, String unifiedFilename )
	{
		this( context, null, targetDirectory, minimumTimeBetweenValidityChecks, sourceExtension, minifiedSourceExtension, unifiedFilename );
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param targetDirectory
	 *        The directory into which unified-minified results should be
	 *        written
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
	public UnifyMinifyFilter( Context context, Restlet next, File targetDirectory, long minimumTimeBetweenValidityChecks, String sourceExtension, String minifiedSourceExtension, String unifiedFilename )
	{
		super( context, next );
		this.sourceExtension = "." + sourceExtension;
		this.minifiedSourceExtension = "." + minifiedSourceExtension;
		this.unifiedFilename = unifiedFilename + this.sourceExtension;
		this.unifiedMinifiedFilename = unifiedFilename + this.minifiedSourceExtension + this.sourceExtension;
		this.targetDirectory = targetDirectory;
		this.minimumTimeBetweenValidityChecks = minimumTimeBetweenValidityChecks;
	}

	//
	// Attributes
	//

	/**
	 * The directories where the sources are found.
	 * <p>
	 * The set is thread-safe.
	 * 
	 * @return The set of source directories
	 */
	public Set<File> getSourceDirectories()
	{
		return sourceDirectories;
	}

	/**
	 * A value of -1 disables all validity checking.
	 * 
	 * @return The minimum time between validity checks in milliseconds
	 * @see #setMinimumTimeBetweenValidityChecks(long)
	 */
	public long getMinimumTimeBetweenValidityChecks()
	{
		return minimumTimeBetweenValidityChecks;
	}

	/**
	 * @param minimumTimeBetweenValidityChecks
	 * @see #getMinimumTimeBetweenValidityChecks()
	 */
	public void setMinimumTimeBetweenValidityChecks( long minimumTimeBetweenValidityChecks )
	{
		this.minimumTimeBetweenValidityChecks = minimumTimeBetweenValidityChecks;
	}

	//
	// Operations
	//

	/**
	 * Unifies all source files in the directory in they are newer than the
	 * target, optionally minifying them as
	 * 
	 * @param targetDirectory
	 *        The directory into which unified-minified results should be
	 *        written
	 * @param minify
	 *        Whether to minify the result
	 * @throws IOException
	 */
	public void unify( File targetDirectory, boolean minify ) throws IOException
	{
		ArrayList<File> sourceFiles = getFiles();
		if( sourceFiles.isEmpty() )
			return;

		File unifiedSourceFile = IoUtil.getUniqueFile( new File( targetDirectory, minify ? unifiedMinifiedFilename : unifiedFilename ) );

		synchronized( unifiedSourceFile )
		{
			if( minify )
				getLogger().info( "Unifying and minifying directories into file \"" + unifiedSourceFile + "\"" );
			else
				getLogger().info( "Unifying directories into file \"" + unifiedSourceFile + "\"" );

			long newLastModified = 0;

			for( File sourceFile : sourceFiles )
			{
				long lastModified = sourceFile.lastModified();
				if( lastModified > newLastModified )
					newLastModified = lastModified;
			}

			if( unifiedSourceFile.lastModified() == newLastModified )
				return;

			if( unifiedSourceFile.exists() )
				if( !unifiedSourceFile.delete() )
					throw new IOException( "Could not delete file: " + unifiedSourceFile );
			unifiedSourceFile.getParentFile().mkdirs();

			Vector<InputStream> ins = new Vector<InputStream>();
			for( File sourceFile : sourceFiles )
			{
				try
				{
					ins.add( new FileInputStream( sourceFile ) );
					ins.add( new ByteArrayInputStream( NEWLINE_BYTES ) );
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
						IoUtil.copyStream( in, out );
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

			if( minify )
				getLogger().info( "Unified and minified directories into file \"" + unifiedSourceFile + "\"" );
			else
				getLogger().info( "Unified directories into file \"" + unifiedSourceFile + "\"" );
		}
	}

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		Reference reference = request.getResourceRef();
		String name = reference.getLastSegment();
		try
		{
			boolean validate = false;
			boolean minify = false;
			if( name.equals( unifiedFilename ) )
				validate = true;
			else if( name.equals( unifiedMinifiedFilename ) )
			{
				validate = true;
				minify = true;
			}

			if( validate )
			{
				long now = System.currentTimeMillis();
				long lastValidityCheck = this.lastValidityCheck.get();
				if( lastValidityCheck == 0 || ( now - lastValidityCheck > minimumTimeBetweenValidityChecks ) )
				{
					if( this.lastValidityCheck.compareAndSet( lastValidityCheck, now ) )
						unify( targetDirectory, minify );
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

	private static final byte[] NEWLINE_BYTES = "\n".getBytes();

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
	 * The directory into which unified-minified results should be written.
	 */
	private final File targetDirectory;

	/**
	 * The directories where the sources are found.
	 */
	private final ConcurrentHashSet<File> sourceDirectories = new ConcurrentHashSet<File>();

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private volatile long minimumTimeBetweenValidityChecks;

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private final AtomicLong lastValidityCheck = new AtomicLong();

	private ArrayList<File> getFiles()
	{
		ArrayList<File> sourceFiles = new ArrayList<File>();
		for( File sourceDirectory : sourceDirectories )
			addFiles( sourceFiles, sourceDirectory );
		Collections.sort( sourceFiles );
		return sourceFiles;
	}

	private void addFiles( ArrayList<File> sourceFiles, File sourceDirectory )
	{
		File[] files = sourceDirectory.listFiles();
		if( files != null )
		{
			for( File sourceFile : files )
			{
				if( sourceFile.isDirectory() )
					addFiles( sourceFiles, sourceFile );
				else
				{
					String name = sourceFile.getName();
					if( name.endsWith( sourceExtension ) && !name.equals( unifiedMinifiedFilename ) && !name.equals( unifiedFilename ) )
						sourceFiles.add( sourceFile );
				}
			}
		}
	}
}
