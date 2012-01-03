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

package com.threecrickets.prudence.service;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.prudence.internal.attributes.ApplicationTaskAttributes;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @see ApplicationTask
 */
public class ApplicationTaskDocumentService extends DocumentService<ApplicationTaskAttributes>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param attributes
	 *        The attributes
	 * @param documentDescriptor
	 *        The initial document descriptor
	 * @param context
	 *        The context made available to the task
	 */
	public ApplicationTaskDocumentService( ApplicationTaskAttributes attributes, DocumentDescriptor<Executable> documentDescriptor, Object context )
	{
		super( attributes, documentDescriptor );
		this.context = context;
	}

	//
	// Attributes
	//

	/**
	 * The context made available to the task.
	 * 
	 * @return The context
	 */
	public Object getContext()
	{
		return context;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The context made available to the task.
	 */
	private final Object context;
}
