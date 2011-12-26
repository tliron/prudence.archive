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

package com.threecrickets.prudence.util;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.util.DefrostTask;

/**
 * A {@link Runnable} that does a GET request on a an internal URI, making sure
 * that the resource is ready to use without the delay of compilation and
 * initializing resources, such as connecting to databases, etc.
 * 
 * @author Tal Liron
 * @see DefrostTask
 */
public class PreheatTask implements Runnable
{
	//
	// Static operations
	//

	/**
	 * Gets the logger used for preheating an application.
	 * 
	 * @param applicationLoggerName
	 *        The application logger name
	 * @return The logger
	 */
	public static Logger getLogger( String applicationLoggerName )
	{
		return LoggingUtil.getSubLogger( LoggingUtil.getLogger( applicationLoggerName ), LOGGER_NAME );
	}

	/**
	 * Creates a preheat task for each document descriptor in a document source.
	 * URIs are assumed to simply be the document names.
	 * 
	 * @param documentSource
	 *        The document source
	 * @param applicationInternalName
	 *        The internal application name
	 * @param application
	 *        The application
	 * @param applicationLoggerName
	 *        The application logger name
	 * @return An array of tasks
	 */
	public static PreheatTask[] forDocumentSource( DocumentSource<Executable> documentSource, String applicationInternalName, Application application, String applicationLoggerName )
	{
		Collection<DocumentDescriptor<Executable>> documentDescriptors = documentSource.getDocuments();
		PreheatTask[] preheatTasks = new PreheatTask[documentDescriptors.size()];
		int i = 0;
		Context context = application.getContext().createChildContext();
		Logger logger = getLogger( applicationLoggerName );
		for( DocumentDescriptor<Executable> documentDescriptor : documentDescriptors )
			preheatTasks[i++] = new PreheatTask( applicationInternalName, documentDescriptor.getDefaultName(), context, logger );

		return preheatTasks;
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param applicationInternalName
	 *        The internal application name
	 * @param resourceUri
	 *        The internal URI
	 * @param application
	 *        The application
	 * @param applicationLoggerName
	 *        The application logger name
	 */
	public PreheatTask( String applicationInternalName, String resourceUri, Application application, String applicationLoggerName )
	{
		this( applicationInternalName, resourceUri, application.getContext().createChildContext(), getLogger( applicationLoggerName ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param applicationInternalName
	 *        The internal application name
	 * @param resourceUri
	 *        The internal URI
	 * @param context
	 *        The context
	 * @param logger
	 *        The logger
	 */
	public PreheatTask( String applicationInternalName, String resourceUri, Context context, Logger logger )
	{
		this.applicationInternalName = applicationInternalName;
		this.resourceUri = resourceUri;
		this.context = context;
		this.logger = logger;
	}

	//
	// Runnable
	//

	public void run()
	{
		String uri = "/" + applicationInternalName + "/" + resourceUri + "/";
		uri = uri.replace( "//", "/" ); // Remove double slashes
		ClientResource clientResource = new ClientResource( context, LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, uri ) );
		try
		{
			long timestamp = System.currentTimeMillis();
			logger.fine( "Preheating: " + uri );
			Representation representation = clientResource.get();
			if( representation != null )
				representation.exhaust();
			logger.fine( "Preheated: " + uri + " (" + ( System.currentTimeMillis() - timestamp ) + ")" );
		}
		catch( ResourceException x )
		{
			if( x.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ) )
				logger.fine( "Could not find resource to preheat: " + uri );
			else
				logger.log( Level.FINE, "Preheating error: " + uri, x );
		}
		catch( IOException x )
		{
			logger.log( Level.FINE, "Preheating error: " + uri, x );
		}
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		if( applicationInternalName != null )
			return applicationInternalName + ", " + resourceUri;
		else
			return resourceUri;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The preheat logger name.
	 */
	private static final String LOGGER_NAME = "preheat";

	/**
	 * The internal application name.
	 */
	private final String applicationInternalName;

	/**
	 * The internal URI.
	 */
	private final String resourceUri;

	/**
	 * The context.
	 */
	private final Context context;

	/**
	 * The logger.
	 */
	private final Logger logger;
}
