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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

import com.threecrickets.prudence.internal.CSSMin;

/**
 * A {@link Filter} that automatically unifies and/or compresses CSS source
 * files, saving them as a single file. Unifying them allows clients to retrieve
 * the CSS via one request rather than many. Compressing them makes them
 * retrievable faster.
 * <p>
 * Compression is done via <a
 * href="http://barryvan.github.com/CSSMin/">CSSMin</a>.
 * <p>
 * This filter can track changes to the source files, updating the result file
 * on-the-fly. This makes it easy to develop and debug a live site.
 * <p>
 * Note that this instances of this class can only guarantee atomic access to
 * the unified/minified version within the current VM.
 * 
 * @author Tal Liron
 */
public class CssUnifyMinifyFilter extends UnifyMinifyFilter
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
	 */
	public CssUnifyMinifyFilter( Context context, File targetDirectory, long minimumTimeBetweenValidityChecks )
	{
		this( context, null, targetDirectory, minimumTimeBetweenValidityChecks );
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
	 */
	public CssUnifyMinifyFilter( Context context, Restlet next, File targetDirectory, long minimumTimeBetweenValidityChecks )
	{
		super( context, next, targetDirectory, minimumTimeBetweenValidityChecks, "css", "min", "all" );
		describe();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void minify( InputStream in, OutputStream out ) throws IOException
	{
		try
		{
			CSSMin.formatFile( new InputStreamReader( in ), out );
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
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( "CssUnifyMinifyFilter" );
		setDescription( "A filter that automatically unifies and/or compresses CSS source files" );
	}
}
