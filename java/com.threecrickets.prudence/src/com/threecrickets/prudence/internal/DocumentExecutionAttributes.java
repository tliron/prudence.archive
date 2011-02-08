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

package com.threecrickets.prudence.internal;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentSource;

public interface DocumentExecutionAttributes
{
	//
	// Attributes
	//

	public LanguageManager getLanguageManager();

	public String getDefaultLanguageTag();

	public boolean isPrepare();

	public DocumentSource<Executable> getLibrariesDocumentSource();

	public DocumentSource<Executable> getCommonLibrariesDocumentSource();

	//
	// Operations
	//

	public String validateDocumentName( String documentName );
}
