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

package com.threecrickets.prudence.service;

import java.io.File;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 */
public class DelegatedHandlerConversationService extends ConversationServiceBase
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param fileUploadSizeThreshold
	 *        The size in bytes beyond which uploaded files will be stored to
	 *        disk
	 * @param fileUploadDirectory
	 *        The directory in which to place uploaded files
	 */
	public DelegatedHandlerConversationService( int fileUploadSizeThreshold, File fileUploadDirectory )
	{
		super( fileUploadSizeThreshold, fileUploadDirectory );
	}
}
