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

package com.threecrickets.prudence.util;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Component;

import com.threecrickets.scripturian.GlobalScope;

/**
 * Utility methods to assist in accessing the running Prudence instance.
 * 
 * @author Tal Liron
 */
public abstract class InstanceUtil
{
	//
	// Constants
	//

	/**
	 * Component attribute for an {@link Application} or a {@link GlobalScope}.
	 */
	public static final String COMPONENT_ATTRIBUTE = "com.threecrickets.prudence.component";

	/**
	 * Applications attribute for a {@link Component}.
	 */
	public static final String APPLICATIONS_ATTRIBUTE = "com.threecrickets.prudence.applications";

	/**
	 * Executor attribute for a {@link Component}.
	 */
	public static final String EXECUTOR_ATTRIBUTE = "com.threecrickets.prudence.executor";

	/**
	 * Cache attribute for a {@link Component} or an {@link Application}.
	 */
	public static final String CACHE_ATTRIBUTE = "com.threecrickets.prudence.cache";

	//
	// Static attributes
	//

	/**
	 * The component for the current Prudence instance.
	 * 
	 * @return The component or null
	 */
	public static Component getComponent()
	{
		return (Component) GlobalScope.getInstance().getAttributes().get( COMPONENT_ATTRIBUTE );
	}

	/**
	 * @param component
	 *        The component
	 * @see #getComponent()
	 */
	public static void setComponent( Component component )
	{
		GlobalScope.getInstance().getAttributes().put( COMPONENT_ATTRIBUTE, component );
	}

	/**
	 * Gets an application associated with the current Prudence instance.
	 * <p>
	 * Expects that a map of applications was set in the
	 * "com.threecrickets.prudence.applications" attribute of the component's
	 * context.
	 * 
	 * @param name
	 *        The application's full name
	 * @return The application or null
	 * @see #getComponent()
	 */
	public static Application getApplication( String name )
	{
		Component component = getComponent();
		if( component != null )
		{
			ConcurrentMap<String, Object> attributes = component.getContext().getAttributes();
			if( attributes != null )
			{
				@SuppressWarnings("unchecked")
				Iterable<Application> applications = (Iterable<Application>) attributes.get( APPLICATIONS_ATTRIBUTE );
				if( applications != null )
					for( Application application : applications )
						if( name.equals( application.getName() ) )
							return application;
			}
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private InstanceUtil()
	{
	}
}
