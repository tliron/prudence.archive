/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.internal.attributes.DelegatedResourceAttributes;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class DelegatedResourceDocumentService extends ResourceDocumentServiceBase<DelegatedResource, DelegatedResourceAttributes>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param documentDescriptor
	 *        The initial document descriptor
	 */
	public DelegatedResourceDocumentService( DelegatedResource resource, DocumentDescriptor<Executable> documentDescriptor )
	{
		super( resource, resource.getAttributes() );
		pushDocumentDescriptor( documentDescriptor );
	}
}