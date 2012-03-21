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

package com.threecrickets.prudence.internal.attributes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;

import com.threecrickets.prudence.DelegatedHandler;
import com.threecrickets.scripturian.Executable;

/**
 * @author Tal Liron
 */
public class DelegatedHandlerAttributes extends VolatileContextualAttributes
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public DelegatedHandlerAttributes( Context context )
	{
		super( DelegatedHandler.class.getCanonicalName() );
		this.context = context;
	}

	//
	// Attributes
	//

	/**
	 * The context.
	 * 
	 * @return The context
	 */
	public Context getContext()
	{
		return context;
	}

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
		entryPointValidityCache = (ConcurrentMap<String, Boolean>) attributes.get( prefix + ".entryPointValidityCache" );
		if( entryPointValidityCache == null )
		{
			entryPointValidityCache = new ConcurrentHashMap<String, Boolean>();
			ConcurrentMap<String, Boolean> existing = (ConcurrentMap<String, Boolean>) attributes.putIfAbsent( prefix + ".entryPointValidityCache", entryPointValidityCache );
			if( existing != null )
				entryPointValidityCache = existing;
		}

		return entryPointValidityCache;
	}

	//
	// ContextualAttributes
	//

	@Override
	public ConcurrentMap<String, Object> getAttributes()
	{
		return context.getAttributes();
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
	private volatile ConcurrentMap<String, Boolean> entryPointValidityCache;
}
