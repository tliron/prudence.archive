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

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public class DelegatedFilterDocumentService extends DocumentServiceBase
{
	//
	// Construction
	//

	public DelegatedFilterDocumentService( DocumentSource<Executable> documentSource )
	{
		super( documentSource );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// DocumentServiceBase
	//

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		return null;
	}
}
