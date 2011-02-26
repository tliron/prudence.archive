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

import com.threecrickets.prudence.DelegatedHandler;
import com.threecrickets.prudence.internal.attributes.DocumentExecutionAttributes;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedHandler
 */
public class DelegatedHandlerDocumentService extends DocumentService
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
	 */
	public DelegatedHandlerDocumentService( DocumentExecutionAttributes attributes, DocumentDescriptor<Executable> documentDescriptor )
	{
		super( attributes );
		pushDocumentDescriptor( documentDescriptor );
	}
}