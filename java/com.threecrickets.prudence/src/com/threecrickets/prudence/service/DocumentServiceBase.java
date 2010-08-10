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

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 */
public abstract class DocumentServiceBase
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param documentSource
	 *        The document source
	 */
	public DocumentServiceBase( DocumentSource<Executable> documentSource )
	{
		this.documentSource = documentSource;
	}

	//
	// Attributes
	//

	/**
	 * The {@link DocumentSource} used to fetch executables.
	 * 
	 * @return The document source
	 */
	public DocumentSource<Executable> getSource()
	{
		return documentSource;
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
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public abstract Representation execute( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException;

	/**
	 * Submits an {@link ApplicationTask} on the the component's executor
	 * service.
	 * 
	 * @param documentName
	 *        The document name
	 * @return A future for the task
	 * @throws ParsingException
	 * @throws DocumentException
	 * @see #getExecutorService()
	 */
	public Future<?> task( String documentName ) throws ParsingException, DocumentException
	{
		ExecutorService executorService = getExecutorService();
		return executorService.submit( new ApplicationTask( documentName ) );
	}

	/**
	 * Access a resource internal to the current application.
	 * 
	 * @param resourceUri
	 *        The URI
	 * @param mediaType
	 *        The default media type of requests
	 * @return The client proxy
	 */
	public ClientResource internal( String resourceUri, String mediaType )
	{
		return new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_APPLICATION, resourceUri ) );
	}

	/**
	 * Access a resource internal to the current Prudence instance.
	 * 
	 * @param applicationInternalName
	 *        The application internal name
	 * @param resourceUri
	 *        The URI
	 * @param mediaType
	 *        The default media type of requests
	 * @return The client proxy
	 */
	public ClientResource internal( String applicationInternalName, String resourceUri, String mediaType )
	{
		return new ClientResource( LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, "/" + applicationInternalName + "/" + resourceUri ) );
	}

	/**
	 * Access any resource.
	 * 
	 * @param uri
	 *        The URI
	 * @param mediaType
	 *        The default media type of requests
	 * @return The client proxy
	 */
	public ClientResource external( String uri, String mediaType )
	{
		return new ClientResource( uri );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document source.
	 */
	private final DocumentSource<Executable> documentSource;

	/**
	 * The executor service.
	 */
	private ExecutorService executorService;

	/**
	 * Gets the executor service for the component, creating one if it doesn't
	 * exist.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.executor</code> in the component's
	 * {@link Context}.
	 * 
	 * @return The executor service
	 */
	private ExecutorService getExecutorService()
	{
		if( executorService == null )
		{
			Application application = Application.getCurrent();
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			executorService = (ExecutorService) attributes.get( "com.threecrickets.prudence.executor" );

			if( executorService == null )
			{
				executorService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2 + 1 );

				ExecutorService existing = (ExecutorService) attributes.putIfAbsent( "com.threecrickets.prudence.executor", executorService );
				if( existing != null )
					executorService = existing;
			}
		}

		return executorService;
	}
}
