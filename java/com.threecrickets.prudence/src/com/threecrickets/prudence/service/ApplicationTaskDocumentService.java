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

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ParsingException;

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
	 * @param documentSource
	 */
	public ApplicationTaskDocumentService( ApplicationTask applicationTask, DocumentSource<Executable> documentSource )
	{
		super( documentSource );
		this.applicationTask = applicationTask;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// DocumentServiceBase
	//

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		documentName = applicationTask.validateDocumentName( documentName );

		DocumentDescriptor<Executable> documentDescriptor;
		try
		{
			documentDescriptor = Executable.createOnce( documentName, getSource(), false, applicationTask.getLanguageManager(), applicationTask.getDefaultLanguageTag(), applicationTask.isPrepare() );
		}
		catch( DocumentNotFoundException x )
		{
			// Try the library directory
			File libraryDirectory = getRelativeFile( applicationTask.getLibraryDirectory() );
			if( libraryDirectory != null )
			{
				try
				{
					documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, getSource(), false, applicationTask.getLanguageManager(), applicationTask.getDefaultLanguageTag(),
						applicationTask.isPrepare() );
				}
				catch( DocumentNotFoundException xx )
				{
					// Try the common library directory
					libraryDirectory = getRelativeFile( applicationTask.getCommonLibraryDirectory() );
					if( libraryDirectory != null )
						documentDescriptor = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, getSource(), false, applicationTask.getLanguageManager(), applicationTask.getDefaultLanguageTag(),
							applicationTask.isPrepare() );
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
	 * The task.
	 */
	private final ApplicationTask applicationTask;
}
