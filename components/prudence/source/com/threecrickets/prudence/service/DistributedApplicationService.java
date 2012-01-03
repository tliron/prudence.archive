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

package com.threecrickets.prudence.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.restlet.Application;

import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.Hazelcast;
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
}
