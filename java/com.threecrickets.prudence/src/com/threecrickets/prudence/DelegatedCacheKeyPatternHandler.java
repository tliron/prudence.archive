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

package com.threecrickets.prudence;

import org.restlet.Context;

/**
 * <p>
 * A {@link DelegatedHandler} with the following supported entry points:
 * <ul>
 * <li><code>handleCacheKeyPattern(conversation, variables)</code></li>
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
	 * @param context
	 *        The context used to configure the handler
	 */
	public DelegatedCacheKeyPatternHandler( String documentName, Context context )
	{
		super( documentName, context );
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
