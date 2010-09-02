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

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import org.restlet.Application;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.prudence.ApplicationTaskCollector;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * A <a href="http://www.sauronsoftware.it/projects/cron4j/">cron4j</a>
 * {@link Task} wrapper for an {@link ApplicationTask}.
 * 
 * @author Tal Liron
 * @see ApplicationTaskCollector
 */
public class ApplicationCronTask extends Task
{
	//
	// Construction
	//

	/**
	 * @param application
	 * @param documentName
	 * @throws ParsingException
	 * @throws DocumentException
	 */
	public ApplicationCronTask( Application application, String documentName ) throws ParsingException, DocumentException
	{
		this( new ApplicationTask( application, documentName ) );
	}

	/**
	 * @param applicationTask
	 */
	public ApplicationCronTask( ApplicationTask applicationTask )
	{
		this.applicationTask = applicationTask;
	}

	//
	// Task
	//

	@Override
	public void execute( TaskExecutionContext context ) throws RuntimeException
	{
		applicationTask.run();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * 
	 */
	private final ApplicationTask applicationTask;
}
