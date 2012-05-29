/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import java.util.Set;

import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.internal.attributes.ResourceContextualAttributes;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @param <R>
 */
public abstract class ResourceDocumentServiceBase<R extends ServerResource, A extends ResourceContextualAttributes> extends DocumentService<A>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param attributes
	 *        The attributes
	 */
	public ResourceDocumentServiceBase( R resource, A attributes )
	{
		super( attributes );
		this.resource = resource;
	}

	//
	// Attributes
	//

	/**
	 * Pass-through documents can exist in {@link #getLibraryDocumentSources()}
	 * as well as in {@link #getDocumentSource()}.
	 * 
	 * @return The pass-through document names
	 */
	public Set<String> getPassThroughDocuments()
	{
		return attributes.getPassThroughDocuments();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The resource.
	 */
	protected final R resource;
}
