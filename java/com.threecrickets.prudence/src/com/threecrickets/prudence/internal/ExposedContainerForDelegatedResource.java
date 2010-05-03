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

package com.threecrickets.prudence.internal;

import java.io.IOException;

import org.restlet.representation.Representation;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * This is the <code>prudence</code> variable exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class ExposedContainerForDelegatedResource extends ExposedContainerBase<DelegatedResource>
{
	//
	// Construction
	//

	/**
	 * Constructs a container with media type and character set according to the
	 * entity representation, or
	 * {@link DelegatedResource#getDefaultCharacterSet()} if none is provided.
	 * 
	 * @param resource
	 *        The resource
	 */
	public ExposedContainerForDelegatedResource( DelegatedResource resource )
	{
		super( resource, resource.getDocumentSource() );
	}

	//
	// Operations
	//

	/**
	 * Executes a source code document. The language of the source code will be
	 * determined by the document tag, which is usually the filename extension.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	@Override
	public Representation execute( String documentName ) throws IOException, ParsingException, ExecutionException
	{
		documentName = resource.validateDocumentName( documentName );

		DocumentDescriptor<Executable> documentDescriptor = Executable
			.createOnce( documentName, resource.getDocumentSource(), false, resource.getLanguageManager(), resource.getDefaultLanguageTag(), resource.isPrepare() );
		Executable executable = documentDescriptor.getDocument();
		executable.execute();

		return null;
	}
}