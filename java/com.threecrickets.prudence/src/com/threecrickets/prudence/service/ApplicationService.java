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

package com.threecrickets.prudence.service;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Application service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 * @see GeneratedTextResource
 */
public class ApplicationService
{
	//
	// Construction
	//

	/**
	 * Construction using the current application.
	 * 
	 * @see Application#getCurrent()
	 */
	public ApplicationService()
	{
		this( Application.getCurrent() );
	}

	/**
	 * Construction.
	 * 
	 * @param application
	 *        The application
	 */
	public ApplicationService( Application application )
	{
		this.application = application;
	}

	//
	// Attributes
	//

	/**
	 * A map of all values global to the current application.
	 * 
	 * @return The globals
	 */
	public ConcurrentMap<String, Object> getGlobals()
	{
		return getApplication().getContext().getAttributes();
	}

	/**
	 * Gets a value global to the current application, atomically setting it to
	 * a default value if it doesn't exist.
	 * 
	 * @param name
	 *        The name of the global
	 * @param defaultValue
	 *        The default value
	 * @return The global's current value
	 */
	public Object getGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		Object value = globals.get( name );
		if( ( value == null ) && ( defaultValue != null ) )
		{
			value = defaultValue;
			Object existing = globals.putIfAbsent( name, value );
			if( existing != null )
				value = existing;
		}
		return value;
	}

	/**
	 * The underlying application.
	 * 
	 * @return The application
	 */
	public Application getApplication()
	{
		return application;
	}

	/**
	 * The application's logger.
	 * 
	 * @return The logger
	 * @see #getSubLogger(String)
	 */
	public Logger getLogger()
	{
		return getApplication().getLogger();
	}

	/**
	 * A logger with a name appended with a "." to the application's logger
	 * name. This allows inheritance of configuration.
	 * 
	 * @param name
	 *        The sub-logger name
	 * @return The logger
	 * @see #getLogger()
	 */
	public Logger getSubLogger( String name )
	{
		return Logger.getLogger( getLogger().getName() + "." + name );
	}

	/**
	 * Get a media type by its MIME type name.
	 * 
	 * @param name
	 *        The MIME type name
	 * @return The media type
	 */
	public MediaType getMediaType( String name )
	{
		MediaType mediaType = MediaType.valueOf( name );
		if( mediaType == null )
			mediaType = getApplication().getMetadataService().getMediaType( name );
		return mediaType;
	}

	/**
	 * Gets the executor service for the component, creating one if it doesn't
	 * exist.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.executor</code> in the component's
	 * {@link Context}.
	 * 
	 * @return The executor service
	 */
	public ExecutorService getGlobalExecutor()
	{
		if( globalExecutor == null )
		{
			Application application = Application.getCurrent();
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			Component component = (Component) attributes.get( "com.threecrickets.prudence.component" );

			if( component == null )
			{
				globalExecutor = application.getTaskService();
			}
			else
			{
				attributes = component.getContext().getAttributes();

				globalExecutor = (ExecutorService) attributes.get( "com.threecrickets.prudence.executor" );

				if( globalExecutor == null )
				{
					globalExecutor = Executors.newScheduledThreadPool( Runtime.getRuntime().availableProcessors() * 2 + 1 );

					ExecutorService existing = (ExecutorService) attributes.putIfAbsent( "com.threecrickets.prudence.executor", globalExecutor );
					if( existing != null )
						globalExecutor = existing;
				}
			}
		}

		return globalExecutor;
	}

	//
	// Operations
	//

	/**
	 * Submits or schedules an {@link ApplicationTask} on the the component's
	 * executor service.
	 * 
	 * @param documentName
	 *        The document name
	 * @param delay
	 *        Initial delay in milliseconds, or zero for ASAP
	 * @param repeatEvery
	 *        Repeat delay in milliseconds, or zero for no repetition
	 * @param fixedRepeat
	 *        Whether repetitions are it fixed times, or if the repeat delay
	 *        begins when the task ends
	 * @return A future for the task
	 * @throws ParsingException
	 * @throws DocumentException
	 * @see #getGlobalExecutor()
	 */
	public Future<?> task( String documentName, int delay, int repeatEvery, boolean fixedRepeat ) throws ParsingException, DocumentException
	{
		ExecutorService executor = getGlobalExecutor();
		if( delay > 0 )
		{
			if( !( executor instanceof ScheduledExecutorService ) )
				throw new RuntimeException( "Global executor must implement the ScheduledExecutorService interface to allow for delayed tasks" );

			ScheduledExecutorService scheduledExecutor = (ScheduledExecutorService) executor;
			if( repeatEvery > 0 )
			{
				if( fixedRepeat )
					return scheduledExecutor.scheduleAtFixedRate( new ApplicationTask( documentName ), delay, repeatEvery, TimeUnit.MILLISECONDS );
				else
					return scheduledExecutor.scheduleWithFixedDelay( new ApplicationTask( documentName ), delay, repeatEvery, TimeUnit.MILLISECONDS );
			}
			else
				return scheduledExecutor.schedule( new ApplicationTask( documentName ), delay, TimeUnit.MILLISECONDS );
		}
		else
			return executor.submit( new ApplicationTask( documentName ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The application.
	 */
	private final Application application;

	/**
	 * The executor service.
	 */
	private ExecutorService globalExecutor;
}
