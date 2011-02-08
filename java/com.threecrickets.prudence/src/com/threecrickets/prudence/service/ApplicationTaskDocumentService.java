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

package com.threecrickets.prudence.service;

import com.threecrickets.prudence.ApplicationTask;

/**
 * @author Tal Liron
 */
public class ApplicationTaskDocumentService extends DocumentServiceBase
{
	//
	// Construction
	//

	/**
	 * @param applicationTask
	 *        The application task
	 */
	public ApplicationTaskDocumentService( ApplicationTask applicationTask )
	{
		super( applicationTask.getAttributes().getDocumentSource(), applicationTask.getAttributes() );
	}
}
