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

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * @author Tal Liron
 */
public class PreheatTask implements Runnable
{
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
		ClientResource clientResource = new ClientResource( context, LocalReference.createRiapReference( LocalReference.RIAP_COMPONENT, "/" + applicationInternalName + "/" + resourceUri ) );
		try
		{
			System.out.println( clientResource.get().getText() );
		}
		catch( ResourceException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Context context;

	private final String applicationInternalName;

	private final String resourceUri;
}
