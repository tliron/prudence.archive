/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.internal;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import com.threecrickets.scripturian.ScriptletController;

public class PrudenceScriptletController<C> implements ScriptletController
{
	//
	// Construction
	//

	public PrudenceScriptletController( C container, String name, ScriptletController scriptletController )
	{
		this.container = container;
		this.name = name;
		this.scriptletController = scriptletController;
	}

	//
	// ScriptletController
	//

	public void initialize( ScriptContext scriptContext ) throws ScriptException
	{
		scriptContext.setAttribute( name, container, ScriptContext.ENGINE_SCOPE );
		if( scriptletController != null )
			scriptletController.initialize( scriptContext );
	}

	public void finalize( ScriptContext scriptContext )
	{
		if( scriptletController != null )
			scriptletController.finalize( scriptContext );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final C container;

	private final String name;

	private final ScriptletController scriptletController;
}
