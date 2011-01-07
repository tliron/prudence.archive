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

import java.io.File;

import com.threecrickets.prudence.DelegatedHandler;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public class DelegatedHandlerDocumentService extends DocumentServiceBase
{
	//
	// Construction
	//

	/**
	 * @param delegatedHandler
	 * @param documentSource
	 */
	public DelegatedHandlerDocumentService( DelegatedHandler delegatedHandler, DocumentSource<Executable> documentSource )
	{
		super( documentSource );
		this.delegatedHandler = delegatedHandler;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// DocumentServiceBase
	//

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		DocumentDescriptor<Executable> documentDescriptor;
		try
		{
			documentDescriptor = Executable.createOnce( documentName, getSource(), false, delegatedHandler.getLanguageManager(), delegatedHandler.getDefaultLanguageTag(), delegatedHandler.isPrepare() );
		}
		catch( DocumentNotFoundException x )
		{
			// Try the library directory
			File libraryDirectory = getRelativeFile( delegatedHandler.getLibraryDirectory() );
			if( libraryDirectory != null )
			{
				try
				{
					documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, getSource(), false, delegatedHandler.getLanguageManager(), delegatedHandler.getDefaultLanguageTag(),
						delegatedHandler.isPrepare() );
				}
				catch( DocumentNotFoundException xx )
				{
					// Try the common library directory
					libraryDirectory = getRelativeFile( delegatedHandler.getCommonLibraryDirectory() );
					if( libraryDirectory != null )
						documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, getSource(), false, delegatedHandler.getLanguageManager(), delegatedHandler.getDefaultLanguageTag(),
							delegatedHandler.isPrepare() );
					else
						throw xx;
				}
			}
			else
				throw x;
		}

		return documentDescriptor;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The handler.
	 */
	private final DelegatedHandler delegatedHandler;
}
