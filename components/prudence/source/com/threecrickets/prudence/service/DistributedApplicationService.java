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

package com.threecrickets.prudence.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.restlet.Application;

import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiTask;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.SerializableApplicationTask;

/**
 * Application service exposed to executables, with the addition of support for
 * distributed tasks.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 * @see GeneratedTextResource
 */
public class DistributedApplicationService extends ApplicationService
{
	//
	// Attributes
	//

	/**
	 * The Hazelcast instance
	 * 
	 * @return The Hazelcast instance
	 * @throw RuntimeException If the Hazelcast instance has not been
	 *        initialized
	 */
	public HazelcastInstance getHazelcast()
	{
		HazelcastInstance hazelcast = Hazelcast.getHazelcastInstanceByName( "com.threecrickets.prudence" );
		if( hazelcast == null )
			throw new RuntimeException( "Cannot find a Hazelcast instance named \"com.threecrickets.prudence\"" );
		return hazelcast;
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
		return getHazelcast().getMap( "com.threecrickets.prudence.distributedGlobals" );
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

	//
	// Operations
	//

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
	public <T> Future<T> distributedExecuteTask( String applicationName, String documentName, String entryPointName, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		return task( new SerializableApplicationTask<T>( applicationName, documentName, entryPointName, context ), where, multi );
	}

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param code
	 *        The code to execute
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
	public <T> Future<T> distributedCodeTask( String applicationName, String code, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		return task( new SerializableApplicationTask<T>( applicationName, code, context ), where, multi );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The application
	 */
	protected DistributedApplicationService( Application application )
	{
		super( application );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param task
	 *        The task
	 * @param where
	 *        A {@link Member}, an iterable of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide
	 * @param multi
	 *        Whether the task should be executed on all members in the set
	 * @return A future for the task
	 * @see Hazelcast#getExecutorService()
	 */
	@SuppressWarnings("unchecked")
	private <T> Future<T> task( SerializableApplicationTask<T> task, Object where, boolean multi )
	{
		ExecutorService executor = getHazelcast().getExecutorService();

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
}
