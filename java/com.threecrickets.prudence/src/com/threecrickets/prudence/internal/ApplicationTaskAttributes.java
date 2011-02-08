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

import org.restlet.Application;
import org.restlet.Context;

import com.threecrickets.prudence.ApplicationTask;

public class ApplicationTaskAttributes extends ContextualAttributes
{
	//
	// Construction
	//

	public ApplicationTaskAttributes( Application application )
	{
		super( ApplicationTask.class );
		this.application = application;
	}

	//
	// ContextualAttributes
	//

	/**
	 * The context.
	 * 
	 * @return The context
	 */
	@Override
	public Context getContext()
	{
		return application.getContext();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Application application;
}
