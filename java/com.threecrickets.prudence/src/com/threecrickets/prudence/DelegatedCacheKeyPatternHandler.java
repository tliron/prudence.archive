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

package com.threecrickets.prudence;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentSource;

/**
 * <p>
 * Supported entry points are:
 * <ul>
 * <li><code>handleCacheKeyPattern()</code></li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class DelegatedCacheKeyPatternHandler extends DelegatedHandler
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param documentName
	 *        The document name
	 * @param documentSource
	 *        The document source
	 * @param languageManager
	 *        The language manager
	 */
	public DelegatedCacheKeyPatternHandler( String documentName, DocumentSource<Executable> documentSource, LanguageManager languageManager )
	{
		super( documentName, documentSource, languageManager );
	}

	//
	// Attributes
	//

	/**
	 * @return The entry point name for <code>handleCacheKeyPattern()</code>
	 * @see #setEntryPointNameForCacheKeyPattern(String)
	 */
	public String getEntryPointNameForCacheKeyPattern()
	{
		return entryPointNameForCacheKeyPattern;
	}

	/**
	 * @param entryPointNameForCacheKeyPattern
	 *        The entry point name for <code>handleCacheKeyPattern()</code>
	 * @see #getEntryPointNameForCacheKeyPattern()
	 */
	public void setEntryPointNameForCacheKeyPattern( String entryPointNameForCacheKeyPattern )
	{
		this.entryPointNameForCacheKeyPattern = entryPointNameForCacheKeyPattern;
	}

	//
	// Operations
	//

	/**
	 * Calls the <code>handleCacheKeyPattern</code> entry point.
	 * 
	 * @param variables
	 *        The cache key pattern variables to handle
	 */
	public void handleCacheKeyPattern( String[] variables )
	{
		handle( entryPointNameForCacheKeyPattern, (Object) variables );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The name of the <code>handleCacheKeyPattern()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForCacheKeyPattern = "handleCacheKeyPattern";
}
