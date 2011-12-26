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

package com.threecrickets.prudence.internal.attributes;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.resource.ServerResource;

/**
 * @author Tal Liron
 */
public class ResourceContextualAttributes extends NonVolatileContextualAttributes
{
	//
	// Construction
	//

	public ResourceContextualAttributes( ServerResource resource )
	{
		super( resource.getClass().getCanonicalName() );
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
	@SuppressWarnings("unchecked")
	public Set<String> getPassThroughDocuments()
	{
		if( passThroughDocuments == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			passThroughDocuments = (Set<String>) attributes.get( prefix + ".passThroughDocuments" );

			if( passThroughDocuments == null )
			{
				passThroughDocuments = new CopyOnWriteArraySet<String>();

				Set<String> existing = (Set<String>) attributes.putIfAbsent( prefix + ".passThroughDocuments", passThroughDocuments );
				if( existing != null )
					passThroughDocuments = existing;
			}
		}

		return passThroughDocuments;
	}

	//
	// ContextualAttributes
	//

	@Override
	public ConcurrentMap<String, Object> getAttributes()
	{
		if( attributes == null )
			attributes = resource.getContext().getAttributes();

		return attributes;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The resource.
	 */
	protected final ServerResource resource;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Pass-through documents can exist in
	 * {@link GeneratedTextResourceAttributes#getLibraryDocumentSources()} as
	 * well as in {@link GeneratedTextResourceAttributes#getDocumentSource()}.
	 */
	private Set<String> passThroughDocuments;

	/**
	 * The attributes.
	 */
	private ConcurrentMap<String, Object> attributes;
}
