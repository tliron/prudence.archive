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

package com.threecrickets.prudence.internal.lazy;

import java.util.HashMap;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;

/**
 * A PHP-style $_POST map.
 * <p>
 * See PHP's <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a>.
 * 
 * @author Tal Liron
 */
public class LazyInitializationPost extends LazyInitializationMap<String, String>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        The request
	 * @param exposedFile
	 *        The exposed file map which will have a chance to consume the
	 *        entity first
	 */
	public LazyInitializationPost( Request request, LazyInitializationFile exposedFile )
	{
		super( new HashMap<String, String>() );
		this.request = request;
		this.exposedFile = exposedFile;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// LazyInitializationMap
	//

	@Override
	protected void initialize()
	{
		if( request.getMethod().equals( Method.POST ) )
		{
			// Give the exposed file map a chance to consume the entity first
			exposedFile.validateInitialized();
			map.putAll( exposedFile.formFields );

			if( request.isEntityAvailable() )
			{
				Form form = new Form( request.getEntity() );
				for( Parameter parameter : form )
					map.put( parameter.getName(), parameter.getValue() );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The request.
	 */
	private final Request request;

	/**
	 * The exposed file map which will have a chance to consume the entity
	 * first.
	 */
	private final LazyInitializationFile exposedFile;
}