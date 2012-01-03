/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.internal.attributes;

import org.restlet.Context;

import com.threecrickets.prudence.SourceCodeResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentSource;

/**
 * @author Tal Liron
 */
public class SourceCodeResourceAttributes extends ResourceContextualAttributes
{
	//
	// Construction
	//

	public SourceCodeResourceAttributes( SourceCodeResource resource )
	{
		super( resource );
	}

	//
	// Attributes
	//

	/**
	 * The {@link DocumentSource} used to fetch documents. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentSources</code> in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	@SuppressWarnings("unchecked")
	public Iterable<DocumentSource<Executable>> getDocumentSources()
	{
		if( documentSources == null )
		{
			documentSources = (Iterable<DocumentSource<Executable>>) getAttributes().get( prefix + ".documentSources" );

			if( documentSources == null )
				throw new RuntimeException( "Attribute " + prefix + ".documentSources must be set in context" );
		}

		return documentSources;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document sources.
	 */
	private Iterable<DocumentSource<Executable>> documentSources;
}
