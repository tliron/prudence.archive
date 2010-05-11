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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;

import com.threecrickets.prudence.internal.LazyInitializationExposedCookie;
import com.threecrickets.prudence.internal.LazyInitializationExposedFile;
import com.threecrickets.prudence.internal.LazyInitializationExposedGet;
import com.threecrickets.prudence.internal.LazyInitializationExposedPost;
import com.threecrickets.prudence.internal.LazyInitializationExposedRequest;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.exception.ExecutionException;

/**
 * An execution controller that exposes PHP-style <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a> ("superglobals") to executables.
 * 
 * @author Tal Liron
 */
public class PhpExecutionController implements ExecutionController
{
	//
	// ExecutionController
	//

	public void initialize( ExecutionContext executionContext ) throws ExecutionException
	{
		Request request = Request.getCurrent();

		LazyInitializationExposedGet exposedGet = new LazyInitializationExposedGet( new HashMap<String, String>(), request );
		LazyInitializationExposedFile exposedFile = new LazyInitializationExposedFile( new HashMap<String, Map<String, Object>>(), request );
		LazyInitializationExposedPost exposedPost = new LazyInitializationExposedPost( new HashMap<String, String>(), request, exposedFile );
		LazyInitializationExposedCookie exposedCookie = new LazyInitializationExposedCookie( new HashMap<String, String>(), request );
		LazyInitializationExposedRequest exposedRequest = new LazyInitializationExposedRequest( new HashMap<String, String>(), exposedGet, exposedPost, exposedCookie );

		// Note that our maps will only contain the last parameter in case of
		// duplicates. This is PHP's behavior.

		executionContext.getExposedVariables().put( "_GET", Collections.unmodifiableMap( exposedGet ) );
		executionContext.getExposedVariables().put( "_FILE", Collections.unmodifiableMap( exposedFile ) );
		executionContext.getExposedVariables().put( "_POST", Collections.unmodifiableMap( exposedPost ) );
		executionContext.getExposedVariables().put( "_COOKIE", Collections.unmodifiableMap( exposedCookie ) );
		executionContext.getExposedVariables().put( "_REQUEST", Collections.unmodifiableMap( exposedRequest ) );
	}

	public void release( ExecutionContext executionContext )
	{
	}
}
