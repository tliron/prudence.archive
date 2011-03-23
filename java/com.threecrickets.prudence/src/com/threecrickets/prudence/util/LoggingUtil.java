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

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.engine.Engine;

/**
 * Utility methods for logging.
 * 
 * @author Tal Liron
 */
public abstract class LoggingUtil
{
	//
	// Static attributes
	//

	/**
	 * Gets a Restlet application logger by Prudence application logger name.
	 * 
	 * @param applicationLoggerName
	 *        The application logger name
	 * @return The logger
	 */
	public static Logger getRestletLogger( String applicationLoggerName )
	{
		return Engine.getLogger( RESTLET_LOGGER_PREFIX + applicationLoggerName );
	}

	/**
	 * Gets a Prudence application logger by Prudence application logger name.
	 * 
	 * @param applicationLoggerName
	 *        The application logger name
	 * @return The logger
	 */
	public static Logger getLogger( String applicationLoggerName )
	{
		return Engine.getLogger( PRUDENCE_LOGGER_PREFIX + applicationLoggerName );
	}

	/**
	 * Gets a Prudence application logger from a Restlet application logger.
	 * 
	 * @param application
	 *        The Restlet application
	 * @return The logger
	 */
	public static Logger getLogger( Application application )
	{
		Logger logger = application.getLogger();

		String name = logger.getName();
		if( name.startsWith( RESTLET_LOGGER_PREFIX ) )
		{
			name = PRUDENCE_LOGGER_PREFIX + name.substring( RESTLET_LOGGER_PREFIX_LENGTH );
			logger = Engine.getLogger( name );
		}

		return logger;
	}

	/**
	 * Gets a sub-logger by appending a "." to a logger's name. This allows
	 * inheritance of configuration.
	 * 
	 * @param logger
	 *        The base logger
	 * @param name
	 *        The sub-logger name
	 * @return The sub-logger
	 */
	public static Logger getSubLogger( Logger logger, String name )
	{
		return Engine.getLogger( logger.getName() + "." + name );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private LoggingUtil()
	{
	}

	/**
	 * Prefix used for the Restlet logger for the application.
	 */
	private static final String RESTLET_LOGGER_PREFIX = "org.restlet.Application.";

	/**
	 * Length of Restlet logger prefix.
	 */
	private static final int RESTLET_LOGGER_PREFIX_LENGTH = RESTLET_LOGGER_PREFIX.length();

	/**
	 * Prefix used for the Prudence logger for the application.
	 */
	private static final String PRUDENCE_LOGGER_PREFIX = "prudence.";
}
