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
import java.io.IOException;

import org.restlet.representation.Representation;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public class ApplicationTaskDocumentService extends DocumentServiceBase
{
	//
	// Construction
	//

	public ApplicationTaskDocumentService( ApplicationTask applicationTask, DocumentSource<Executable> documentSource )
	{
		super( documentSource );
		this.applicationTask = applicationTask;
	}

	//
	// DocumentServiceBase
	//

	@Override
	public Representation execute( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = applicationTask.validateDocumentName( documentName );

		Executable executable;
		try
		{
			executable = Executable.createOnce( documentName, applicationTask.getDocumentSource(), false, applicationTask.getLanguageManager(), applicationTask.getDefaultLanguageTag(), applicationTask.isPrepare() )
				.getDocument();
		}
		catch( DocumentNotFoundException x )
		{
			// Try the library directory
			File libraryDirectory = applicationTask.getLibraryDirectoryRelative();
			if( libraryDirectory != null )
				executable = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, applicationTask.getDocumentSource(), false, applicationTask.getLanguageManager(),
					applicationTask.getDefaultLanguageTag(), applicationTask.isPrepare() ).getDocument();
			else
				throw x;
		}

		executable.execute();

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ApplicationTask applicationTask;
}
