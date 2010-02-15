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

import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentDescriptor;
import com.threecrickets.scripturian.DocumentSource;

/**
 * @author Tal Liron
 */
public class PreheatTask implements Runnable
{
	//
	// Static operations
	//

	/**
	 * @param context
	 * @param applicationInternalName
	 * @param documentSource
	 * @return
	 */
	public static PreheatTask[] create( Context context, String applicationInternalName, DocumentSource<Document> documentSource )
	{
		Collection<DocumentDescriptor<Document>> documentDescriptors = documentSource.getDocumentDescriptors();
		PreheatTask[] preheatTasks = new PreheatTask[documentDescriptors.size()];
		int i = 0;
		for( DocumentDescriptor<Document> documentDescriptor : documentDescriptors )
			preheatTasks[i++] = new PreheatTask( context, applicationInternalName, documentDescriptor.getDefaultName() );

		return preheatTasks;
	}

	//
	// Construction
	//

	/**
	 * @param context
	 * @param applicationInternalName
	 * @param resourceUri
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
		String uri = "/" + applicationInternalName + "/" + resourceUri;
		uri = uri.replace( "//", "/" ); // Remove double slashes
		context.getLogger().info( "Preheating: " + uri );
		ClientResource clientResource = new ClientResource( context, LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, uri ) );
		try
		{
			clientResource.get();
			// System.out.println( clientResource.get().getText() );
		}
		catch( ResourceException e )
		{
			System.err.println( uri );
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Context context;

	private final String applicationInternalName;

	private final String resourceUri;
}
