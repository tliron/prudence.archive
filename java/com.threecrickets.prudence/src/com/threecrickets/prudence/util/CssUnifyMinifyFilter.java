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
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param scriptsDirectory
	 *        The directory where the source scripts are found
	 * @param minimumTimeBetweenValidityChecks
	 *        See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	public CssUnifyMinifyFilter( Context context, Restlet next, File scriptsDirectory, long minimumTimeBetweenValidityChecks )
	{
		super( context, next, scriptsDirectory, minimumTimeBetweenValidityChecks, "css", "min", "all" );
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
}
