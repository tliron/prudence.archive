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

package com.threecrickets.prudence.util;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.io.IOException;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public class PrudenceCronTask extends Task
{
	//
	// Construction
	//

	public PrudenceCronTask( Executable executable )
	{
		this.executable = executable;
	}

	//
	// Task
	//

	@Override
	public void execute( TaskExecutionContext context ) throws RuntimeException
	{
		try
		{
			ExecutionContext executionContext = new ExecutionContext();
			executable.execute( executionContext );
		}
		catch( ParsingException x )
		{
			throw new RuntimeException( x );
		}
		catch( ExecutionException x )
		{
			throw new RuntimeException( x );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Executable executable;
}