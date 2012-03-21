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

package com.threecrickets.prudence.internal;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import org.restlet.Application;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.prudence.ApplicationTaskCollector;

/**
 * A <a href="http://www.sauronsoftware.it/projects/cron4j/">cron4j</a>
 * {@link Task} wrapper for an {@link ApplicationTask}.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
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
	 * Constructor.
	 * 
	 * @param application
	 *        The Restlet application in which this task will execute
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task
	 */
	public ApplicationCronTask( Application application, String documentName, String entryPointName, Object context )
	{
		this( new ApplicationTask<Void>( application, documentName, entryPointName, context ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param applicationTask
	 *        The application task
	 */
	public ApplicationCronTask( ApplicationTask<?> applicationTask )
	{
		this.applicationTask = applicationTask;
	}

	//
	// Task
	//

	@Override
	public void execute( TaskExecutionContext context ) throws RuntimeException
	{
		applicationTask.call();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The application task.
	 */
	private final ApplicationTask<?> applicationTask;
}
