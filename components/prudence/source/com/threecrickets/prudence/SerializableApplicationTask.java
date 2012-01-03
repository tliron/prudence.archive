/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.restlet.Application;

import com.threecrickets.prudence.util.InstanceUtil;

/**
 * A serializable wrapper for an {@link ApplicationTask}.
 * 
 * @author Tal Liron
 * @see ApplicationTask
 * @see InstanceUtil#getApplication(String)
 * @param <T>
 *        The return type
 */
public class SerializableApplicationTask<T> implements Callable<T>, Serializable
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param applicationName
	 *        The full name of the Restlet application in which this task will
	 *        execute
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task
	 */
	public SerializableApplicationTask( String applicationName, String documentName, String entryPointName, Object context )
	{
		this.applicationName = applicationName;
		this.documentName = documentName;
		this.entryPointName = entryPointName;
		this.context = context;
	}

	//
	// Attributes
	//

	/**
	 * Gets the application task.
	 * 
	 * @return The application task or null if the application was not found
	 * @see InstanceUtil#getApplication(String)
	 */
	public ApplicationTask<T> getApplicationTask()
	{
		if( applicationTask == null )
		{
			Application application = InstanceUtil.getApplication( applicationName );
			if( application != null )
				applicationTask = new ApplicationTask<T>( application, documentName, entryPointName, context );
		}

		return applicationTask;
	}

	//
	// Callable
	//

	public T call()
	{
		ApplicationTask<T> applicationTask = getApplicationTask();
		if( applicationTask == null )
			throw new RuntimeException( "Could not find an application named: " + applicationName );

		return applicationTask.call();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	/**
	 * The full name of the Restlet application in which this task will execute.
	 */
	private String applicationName;

	/**
	 * The document name.
	 */
	private String documentName;

	/**
	 * The entry point name.
	 */
	private String entryPointName;

	/**
	 * The context made available to the task.
	 */
	private Object context;

	/**
	 * Cache for the generated application task.
	 */
	private transient ApplicationTask<T> applicationTask;
}
