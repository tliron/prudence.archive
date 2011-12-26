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

import com.threecrickets.scripturian.LanguageAdapter;
import com.threecrickets.scripturian.ScriptletPlugin;

/**
 * <p>
 * A {@link DelegatedHandler} with the following supported entry points:
 * <ul>
 * <li><code>getScriptlet(code, content)</code></li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class DelegatedScriptletPlugin extends DelegatedHandler implements ScriptletPlugin
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param documentName
	 *        The document name
	 * @param context
	 *        The context used to configure the handler
	 */
	public DelegatedScriptletPlugin( String documentName, Context context )
	{
		super( documentName, context );
	}

	//
	// Attributes
	//

	/**
	 * @return The entry point name for <code>getScriptlet()</code>
	 * @see #setEntryPointNameForGetScriptlet(String)
	 */
	public String getEntryPointNameForGetScriptlet()
	{
		return entryPointNameForGetScriptlet;
	}

	/**
	 * @param entryPointNameForGetScriptlet
	 *        The entry point name for <code>getScriptlet()</code>
	 * @see #getEntryPointNameForGetScriptlet()
	 */
	public void setEntryPointNameForGetScriptlet( String entryPointNameForGetScriptlet )
	{
		this.entryPointNameForGetScriptlet = entryPointNameForGetScriptlet;
	}

	//
	// ScriptletPlugin
	//

	public String getScriptlet( String code, LanguageAdapter languageAdapter, String content )
	{
		Object r = handle( entryPointNameForGetScriptlet, code, languageAdapter, content );
		return r != null ? r.toString() : null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The name of the <code>handleGetScriptlet()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForGetScriptlet = "handleGetScriptlet";
}
