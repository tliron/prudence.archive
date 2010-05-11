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
import java.util.concurrent.atomic.AtomicLong;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

import com.threecrickets.prudence.internal.JavaScriptUnifyMinify;

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
 * 
 * @author Tal Liron
 */
public class JavaScriptUnifyMinifyFilter extends Filter
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
	public JavaScriptUnifyMinifyFilter( Context context, Restlet next, File scriptsDirectory, long minimumTimeBetweenValidityChecks )
	{
		super( context, next );
		this.scriptsDirectory = scriptsDirectory;
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
			if( path.endsWith( JavaScriptUnifyMinify.ALL ) )
				validate = true;
			else if( path.endsWith( JavaScriptUnifyMinify.ALL_MIN ) )
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

					JavaScriptUnifyMinify.unify( new File( scriptsDirectory, path ).getParentFile(), minify );
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
	// Private

	/**
	 * The directory where the source scripts are found.
	 */
	private final File scriptsDirectory;

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private final AtomicLong minimumTimeBetweenValidityChecks = new AtomicLong();

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private final AtomicLong lastValidityCheck = new AtomicLong();
}
