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

package com.threecrickets.prudence.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;

import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiTask;
import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.SerializableApplicationTask;
import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.prudence.util.LoggingUtil;

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
	 * Constructor using the current application.
	 * 
	 * @see Application#getCurrent()
	 */
	public ApplicationService()
	{
		this( Application.getCurrent() );
	}

	/**
	 * Constructor.
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
	 * The underlying application.
	 * 
	 * @return The application
	 */
	public Application getApplication()
	{
		return application;
	}

	/**
	 * The underlying component.
	 * <p>
	 * Note: for this to work, the component must have been explicitly set as
	 * attribute <code>com.threecrickets.prudence.component</code> in the
	 * application's context.
	 * 
	 * @return The component
	 */
	public Component getComponent()
	{
		return (Component) getGlobals().get( InstanceUtil.COMPONENT_ATTRIBUTE );
	}

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

		if( defaultValue != null )
		{
			value = defaultValue;
			Object existing = globals.putIfAbsent( name, value );
			if( existing != null )
				value = existing;
		}
		else
			globals.remove( name );

		return value;
	}

	/**
	 * A map of all values global to all running applications.
	 * <p>
	 * Note that this could be null if shared globals are not set up.
	 * 
	 * @return The shared globals or null
	 * @see #getComponent()
	 */
	public ConcurrentMap<String, Object> getSharedGlobals()
	{
		Component component = getComponent();
		if( component != null )
			return component.getContext().getAttributes();
		else
			return null;
	}

	/**
	 * Gets a value global to all running applications, atomically setting it to
	 * a default value if it doesn't exist.
	 * <p>
	 * If shared globals are not set up, does nothing and returns null.
	 * 
	 * @param name
	 *        The name of the shared global
	 * @param defaultValue
	 *        The default value
	 * @return The shared global's current value
	 * @see #getComponent()
	 */
	public Object getSharedGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> globals = getSharedGlobals();

		if( globals == null )
			return null;

		Object value = globals.get( name );

		if( value == null )
		{
			if( defaultValue != null )
			{
				value = defaultValue;
				Object existing = globals.putIfAbsent( name, value );
				if( existing != null )
					value = existing;
			}
			else
				globals.remove( name );
		}

		return value;
	}

	/**
	 * A map of all values global to the Prudence Hazelcast cluster.
	 * <p>
	 * This is simply the "com.threecrickets.prudence.distributedGlobals"
	 * Hazelcast map.
	 * 
	 * @return The distributed globals or null
	 */
	public ConcurrentMap<String, Object> getDistributedGlobals()
	{
		return Hazelcast.getMap( "com.threecrickets.prudence.distributedGlobals" );
	}

	/**
	 * Gets a value global to the Prudence Hazelcast cluster, atomically setting
	 * it to a default value if it doesn't exist.
	 * <p>
	 * If distributed globals are not set up, does nothing and returns null.
	 * 
	 * @param name
	 *        The name of the distributed global
	 * @param defaultValue
	 *        The default value
	 * @return The distributed global's current value
	 */
	public Object getDistributedGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> globals = getDistributedGlobals();

		if( globals == null )
			return null;

		Object value = globals.get( name );

		if( value == null )
		{
			if( defaultValue != null )
			{
				value = defaultValue;
				Object existing = globals.putIfAbsent( name, value );
				if( existing != null )
					value = existing;
			}
			else
				globals.remove( name );
		}

		return value;
	}

	/**
	 * The application's logger.
	 * 
	 * @return The logger
	 * @see #getSubLogger(String)
	 */
	public Logger getLogger()
	{
		if( logger == null )
			logger = LoggingUtil.getLogger( application );

		return logger;
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
		return LoggingUtil.getSubLogger( getLogger(), name );
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
	 * Gets the shared executor service, creating one if it doesn't exist.
	 * <p>
	 * If shared globals are not set up, gets the application's executor
	 * service.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.executor</code> in the component's
	 * {@link Context}.
	 * 
	 * @return The executor service
	 */
	public ExecutorService getExecutor()
	{
		if( executor == null )
		{
			ConcurrentMap<String, Object> attributes = getSharedGlobals();

			if( attributes != null )
			{
				executor = (ExecutorService) attributes.get( InstanceUtil.EXECUTOR_ATTRIBUTE );

				if( executor == null )
				{
					executor = Executors.newScheduledThreadPool( Runtime.getRuntime().availableProcessors() * 2 + 1 );

					ExecutorService existing = (ExecutorService) attributes.putIfAbsent( InstanceUtil.EXECUTOR_ATTRIBUTE, executor );
					if( existing != null )
						executor = existing;
				}
			}
			else
				executor = application.getTaskService();
		}

		return executor;
	}

	//
	// Operations
	//

	/**
	 * Submits or schedules an {@link ApplicationTask} on the the shared
	 * executor service.
	 * 
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task
	 * @param delay
	 *        Initial delay in milliseconds, or zero for ASAP
	 * @param repeatEvery
	 *        Repeat delay in milliseconds, or zero for no repetition
	 * @param fixedRepeat
	 *        Whether repetitions are at fixed times, or if the repeat delay
	 *        begins when the task ends
	 * @return A future for the task
	 * @see #getExecutor()
	 */
	@SuppressWarnings("unchecked")
	public <T> Future<T> task( String applicationName, String documentName, String entryPointName, Object context, int delay, int repeatEvery, boolean fixedRepeat )
	{
		Application application = this.application;
		if( applicationName != null )
			application = InstanceUtil.getApplication( applicationName );

		ExecutorService executor = getExecutor();
		ApplicationTask<T> task = new ApplicationTask<T>( application, documentName, entryPointName, context );
		if( ( delay > 0 ) || ( repeatEvery > 0 ) )
		{
			if( !( executor instanceof ScheduledExecutorService ) )
				throw new RuntimeException( "Executor must implement the ScheduledExecutorService interface to allow for delayed tasks" );

			ScheduledExecutorService scheduledExecutor = (ScheduledExecutorService) executor;
			if( repeatEvery > 0 )
			{
				if( fixedRepeat )
					return (ScheduledFuture<T>) scheduledExecutor.scheduleAtFixedRate( task, delay, repeatEvery, TimeUnit.MILLISECONDS );
				else
					return (ScheduledFuture<T>) scheduledExecutor.scheduleWithFixedDelay( task, delay, repeatEvery, TimeUnit.MILLISECONDS );
			}
			else
				return (ScheduledFuture<T>) scheduledExecutor.schedule( (Callable<T>) task, delay, TimeUnit.MILLISECONDS );
		}
		else
			return (Future<T>) executor.submit( (Callable<T>) task );
	}

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task (must be serializable)
	 * @param where
	 *        A {@link Member}, an iterable of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide
	 * @param multi
	 *        Whether the task should be executed on all members in the set
	 * @return A future for the task
	 * @see Hazelcast#getExecutorService()
	 */
	@SuppressWarnings("unchecked")
	public <T> Future<T> distributedTask( String applicationName, String documentName, String entryPointName, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		ExecutorService executor = Hazelcast.getExecutorService();
		SerializableApplicationTask<T> task = new SerializableApplicationTask<T>( applicationName, documentName, entryPointName, context );

		DistributedTask<T> distributedTask;
		if( where == null )
			distributedTask = new DistributedTask<T>( task );
		else if( where instanceof Member )
			distributedTask = new DistributedTask<T>( task, (Member) where );
		else if( where instanceof Set )
		{
			if( multi )
				distributedTask = new MultiTask<T>( task, (Set<Member>) where );
			else
				distributedTask = new DistributedTask<T>( task, (Set<Member>) where );
		}
		else if( where instanceof Iterable )
		{
			Set<Member> members = new HashSet<Member>();
			for( Member member : (Iterable<Member>) where )
				members.add( member );
			if( multi )
				distributedTask = new MultiTask<T>( task, members );
			else
				distributedTask = new DistributedTask<T>( task, members );
		}
		else
			distributedTask = new DistributedTask<T>( task, where );

		return (Future<T>) executor.submit( distributedTask );
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
	private ExecutorService executor;

	/**
	 * The logger.
	 */
	private Logger logger;
}
