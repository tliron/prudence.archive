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

package com.threecrickets.prudence.internal;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;

import com.threecrickets.prudence.ApplicationTask;

public class ApplicationTaskAttributes extends NonVolatileContextualAttributes
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param application
	 *        The application
	 */
	public ApplicationTaskAttributes( Application application )
	{
		super( ApplicationTask.class.getCanonicalName() );
		this.application = application;
	}

	//
	// ContextualAttributes
	//

	@Override
	public ConcurrentMap<String, Object> getAttributes()
	{
		return application.getContext().getAttributes();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The application.
	 */
	private final Application application;
}
