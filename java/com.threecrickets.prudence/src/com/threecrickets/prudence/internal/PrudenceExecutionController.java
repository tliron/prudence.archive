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

package com.threecrickets.prudence.internal;

import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.exception.ExecutionException;

/**
 * Exposes Prudence container to executables.
 * 
 * @author Tal Liron
 * @param <C>
 */
public class PrudenceExecutionController<C> implements ExecutionController
{
	//
	// Construction
	//

	/**
	 * @param container
	 * @param name
	 * @param executionController
	 */
	public PrudenceExecutionController( C container, String name, ExecutionController executionController )
	{
		this.container = container;
		this.name = name;
		this.executionController = executionController;
	}

	//
	// ExecutionController
	//

	public void initialize( ExecutionContext executionContext ) throws ExecutionException
	{
		oldContainer = executionContext.getExposedVariables().put( name, container );
		if( executionController != null )
			executionController.initialize( executionContext );
	}

	public void finalize( ExecutionContext executionContext )
	{
		if( executionController != null )
			executionController.finalize( executionContext );
		if( oldContainer != null )
			executionContext.getExposedVariables().put( name, oldContainer );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * 
	 */
	private final C container;

	/**
	 * 
	 */
	private Object oldContainer;

	/**
	 * 
	 */
	private final String name;

	/**
	 * 
	 */
	private final ExecutionController executionController;
}
