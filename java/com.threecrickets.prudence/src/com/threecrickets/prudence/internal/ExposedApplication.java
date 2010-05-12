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

import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.data.MediaType;

/**
 * Application services exposed to executables.
 * 
 * @author Tal Liron
 */
public class ExposedApplication
{
	//
	// Attributes
	//

	/**
	 * A map of all values global to the current applications.
	 * 
	 * @return The globals
	 */
	public ConcurrentMap<String, Object> getGlobals()
	{
		return Application.getCurrent().getContext().getAttributes();
	}

	/**
	 * Gets a value global to the current application.
	 * 
	 * @param name
	 *        The name of the global
	 * @return The global's current value
	 */
	public Object getGlobal( String name )
	{
		return getGlobal( name, null );
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
	 * Sets the value global to the current application.
	 * 
	 * @param name
	 *        The name of the global
	 * @param value
	 *        The global's new value
	 * @return The global's previous value
	 */
	public Object setGlobal( String name, Object value )
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		return globals.put( name, value );
	}

	/**
	 * The underlying application.
	 * 
	 * @return The application
	 */
	public Application getApplication()
	{
		return Application.getCurrent();
	}

	/**
	 * The application's logger.
	 * 
	 * @return The logger
	 */
	public Logger getLogger()
	{
		return getApplication().getLogger();
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
}
