/**
 * Copyright 2009-2012 Three Crickets LLC.
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
import java.io.OutputStream;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

import com.threecrickets.prudence.internal.JSMin;

/**
 * A {@link Filter} that automatically unifies and/or compresses JavaScript
 * source files, saving them as a single file. Unifying them allows clients to
 * retrieve the JavaScript via one request rather than many. Compressing them
 * makes them retrievable faster.
 * <p>
 * Compression is done via <a
 * href="http://www.inconspicuous.org/projects/jsmin/jsmin.java">John Reilly's
 * Java port</a> of Douglas Crockford's <a
 * href="http://www.crockford.com/javascript/jsmin.html">JSMin</a>.
 * <p>
 * This filter can track changes to the source files, updating the result file
 * on-the-fly. This makes it easy to develop and debug a live site.
 * <p>
 * Note that this instances of this class can only guarantee atomic access to
 * the unified/minified version within the current VM.
 * 
 * @author Tal Liron
 */
public class JavaScriptUnifyMinifyFilter extends UnifyMinifyFilter
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param scriptsDirectory
	 *        The directory where the source scripts are found
	 * @param minimumTimeBetweenValidityChecks
	 *        See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	public JavaScriptUnifyMinifyFilter( Context context, File scriptsDirectory, long minimumTimeBetweenValidityChecks )
	{
		this( context, null, scriptsDirectory, minimumTimeBetweenValidityChecks );
	}

	/**
	 * Constructor.
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
	public JavaScriptUnifyMinifyFilter( Context context, Restlet next, File scriptsDirectory, long minimumTimeBetweenValidityChecks )
	{
		super( context, next, scriptsDirectory, minimumTimeBetweenValidityChecks, "js", "min", "all" );
		describe();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void minify( InputStream in, OutputStream out ) throws IOException
	{
		JSMin jsMin = new JSMin( in, out );
		try
		{
			jsMin.jsmin();
		}
		catch( IOException x )
		{
			throw x;
		}
		catch( Exception x )
		{
			IOException iox = new IOException();
			iox.initCause( x );
			throw iox;
		}
	}

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Tal Liron" );
		setName( "JavaScriptUnifyMinifyFilter" );
		setDescription( "A filter that automatically unifies and/or compresses JavaScript source files" );
	}
}
