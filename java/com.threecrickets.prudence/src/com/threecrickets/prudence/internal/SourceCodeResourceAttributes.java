package com.threecrickets.prudence.internal;

import org.restlet.Context;

import com.threecrickets.prudence.SourceCodeResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentSource;

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
			documentSources = (Iterable<DocumentSource<Executable>>) getAttributes().get( prefix + "documentSources" );

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
