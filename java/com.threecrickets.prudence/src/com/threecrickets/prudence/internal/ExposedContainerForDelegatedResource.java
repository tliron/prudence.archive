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

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageAdapter;
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
	 * As {@link #includeDocument(String)}, except that the document is parsed
	 * as a single, non-delimited script with the engine name derived from
	 * name's extension.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	@Override
	public Representation include( String documentName ) throws IOException, ParsingException, ExecutionException
	{
		if( resource.isTrailingSlashRequired() )
		{
			if( ( documentName != null ) && ( documentName.length() != 0 ) && !documentName.endsWith( "/" ) )
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
		}

		if( ( documentName == null ) || ( documentName.length() == 0 ) || ( documentName.equals( "/" ) ) )
			documentName = resource.getDefaultName();

		DocumentDescriptor<Executable> documentDescriptor = resource.getDocumentSource().getDocument( documentName );

		Executable executable = documentDescriptor.getDocument();
		if( executable == null )
		{
			LanguageAdapter languageAdapter = resource.getLanguageManager().getAdapterByExtension( documentName, documentDescriptor.getTag() );
			String sourceCode = documentDescriptor.getSourceCode();
			executable = new Executable( documentDescriptor.getDefaultName(), sourceCode, false, resource.getLanguageManager(), (String) languageAdapter.getAttributes().get( LanguageAdapter.DEFAULT_TAG ), resource
				.getDocumentSource(), resource.isPrepare() );

			Executable existing = documentDescriptor.setDocumentIfAbsent( executable );
			if( existing != null )
				executable = existing;
		}

		executable.execute();

		return null;
	}
}