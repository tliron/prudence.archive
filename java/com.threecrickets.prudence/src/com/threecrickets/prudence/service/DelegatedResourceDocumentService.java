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

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
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
	 */
	public DelegatedResourceDocumentService( DelegatedResource resource )
	{
		super( resource, resource.getDocumentSource() );
	}

	//
	// ResourceDocumentService
	//

	/**
	 * Executes a source code document. The language of the source code will be
	 * determined by the document tag, which is usually the filename extension.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	@Override
	public Representation execute( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = resource.validateDocumentName( documentName );

		Executable executable;
		try
		{
			executable = Executable.createOnce( documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() ).getDocument();
		}
		catch( DocumentNotFoundException x )
		{
			// Try the library directory
			File libraryDirectory = resource.getLibraryDirectoryRelative();
			if( libraryDirectory != null )
				executable = Executable.createOnce( libraryDirectory.getPath() + "/" + documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(),
					resource.isPrepare() ).getDocument();
			else
				throw x;
		}

		executable.execute();

		return null;
	}
}