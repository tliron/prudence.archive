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

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class DelegatedResourceDocumentService extends ResourceDocumentServiceBase<DelegatedResource>
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
		super( resource, resource.getDocumentSource() );
		pushDocumentDescriptor( documentDescriptor );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// DocumentServiceBase
	//

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		documentName = resource.validateDocumentName( documentName );

		DocumentDescriptor<Executable> documentDescriptor;
		try
		{
			documentDescriptor = Executable.createOnce( documentName, getSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		}
		catch( DocumentNotFoundException x )
		{
			// Try the library directory
			File libraryDirectory = resource.getRelativeFile( resource.getLibraryDirectory() );
			if( libraryDirectory != null )
			{
				try
				{
					documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, getSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
				}
				catch( DocumentNotFoundException xx )
				{
					// Try the common library directory
					libraryDirectory = resource.getRelativeFile( resource.getCommonLibraryDirectory() );
					if( libraryDirectory != null )
						documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, getSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(),
							resource.isPrepare() );
					else
						throw xx;
				}
			}
			else
				throw x;
		}

		return documentDescriptor;
	}
}