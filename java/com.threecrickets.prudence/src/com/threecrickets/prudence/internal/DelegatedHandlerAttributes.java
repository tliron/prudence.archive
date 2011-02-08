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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;

import com.threecrickets.prudence.DelegatedHandler;
import com.threecrickets.scripturian.Executable;

public class DelegatedHandlerAttributes extends ContextualAttributes
{
	//
	// Construction
	//

	public DelegatedHandlerAttributes( Context context )
	{
		super( DelegatedHandler.class );
		this.context = context;
	}

	//
	// Attributes
	//

	/**
	 * A cache for entry point validity.
	 * 
	 * @param executable
	 *        The executable
	 * @return The entry point validity cache
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, Boolean> getEntryPointValidityCache( Executable executable )
	{
		ConcurrentMap<String, Object> attributes = executable.getAttributes();
		entryPointValidityCache = (ConcurrentMap<String, Boolean>) attributes.get( "com.threecrickets.prudence.DelegatedHandler.entryPointValidityCache" );
		if( entryPointValidityCache == null )
		{
			entryPointValidityCache = new ConcurrentHashMap<String, Boolean>();
			ConcurrentMap<String, Boolean> existing = (ConcurrentMap<String, Boolean>) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedHandler.entryPointValidityCache", entryPointValidityCache );
			if( existing != null )
				entryPointValidityCache = existing;
		}

		return entryPointValidityCache;
	}

	//
	// ContextualAttributes
	//

	/**
	 * The context.
	 * 
	 * @return The context
	 */
	@Override
	public Context getContext()
	{
		return context;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The context.
	 */
	private final Context context;

	/**
	 * A cache for entry point validity.
	 */
	private ConcurrentMap<String, Boolean> entryPointValidityCache;
}
