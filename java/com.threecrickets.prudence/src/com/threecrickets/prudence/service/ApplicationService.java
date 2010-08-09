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
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.data.MediaType;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;

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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The application.
	 */
	private final Application application;
}
