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

package com.threecrickets.prudence.util;

import java.util.Collection;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.DefrostTask;
import com.threecrickets.scripturian.DocumentDescriptor;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.Executable;

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
	 * Creates a preheat task for each document descriptor in a document source.
	 * URIs are assumed to simply be the document names.
	 * 
	 * @param documentSource
	 *        The document source
	 * @param context
	 *        The context
	 * @param applicationInternalName
	 *        The internal application name
	 * @return An array of tasks
	 */
	public static PreheatTask[] forDocumentSource( DocumentSource<Executable> documentSource, Context context, String applicationInternalName )
	{
		Collection<DocumentDescriptor<Executable>> documentDescriptors = documentSource.getDocuments();
		PreheatTask[] preheatTasks = new PreheatTask[documentDescriptors.size()];
		int i = 0;
		for( DocumentDescriptor<Executable> documentDescriptor : documentDescriptors )
			preheatTasks[i++] = new PreheatTask( context, applicationInternalName, documentDescriptor.getDefaultName() );

		return preheatTasks;
	}

	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param context
	 *        The context
	 * @param applicationInternalName
	 *        The internal application name
	 * @param resourceUri
	 *        The internal URI
	 */
	public PreheatTask( Context context, String applicationInternalName, String resourceUri )
	{
		this.context = context;
		this.applicationInternalName = applicationInternalName;
		this.resourceUri = resourceUri;
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
			context.getLogger().fine( "Preheating: " + uri );
			clientResource.get();
			context.getLogger().fine( "Preheated: " + uri );
			/*
			 * try { System.out.println( clientResource.get().getText() ); }
			 * catch( IOException x ) { }
			 */
		}
		catch( ResourceException x )
		{
			if( x.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ) )
			{
				context.getLogger().warning( "Could not find resource to preheat: " + uri );
				System.err.println( "Could not find resource to preheat: " + clientResource.getReference() );
			}
			else
			{
				context.getLogger().log( Level.SEVERE, "Preheating error: " + uri, x );
				System.err.print( clientResource.getReference() + " - " );
				x.printStackTrace();
			}
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
	 * The context.
	 */
	private final Context context;

	/**
	 * The internal application name.
	 */
	private final String applicationInternalName;

	/**
	 * The internal URI.
	 */
	private final String resourceUri;
}
