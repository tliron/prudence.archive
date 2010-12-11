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

package com.threecrickets.prudence;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.restlet.Application;

import com.threecrickets.prudence.internal.ApplicationCronTask;

/**
 * A <a href="http://www.sauronsoftware.it/projects/cron4j/">cron4j</a>
 * {@link TaskCollector} designed to poll a crontab-like file every minute.
 * <p>
 * The syntax of the crontab-like file is simple. Each line starts with a
 * {@link SchedulingPattern}, and after whitespace is a document name. This line
 * becomes a {@link ApplicationTask}, which executes that document.
 * <p>
 * Empty lines and comment lines (beginning with a "#") are ignored.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 * @see ApplicationTask
 */
public class ApplicationTaskCollector implements TaskCollector
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param crontab
	 *        The crontab file
	 * @param application
	 *        The Restlet application in which tasks will execute
	 */
	public ApplicationTaskCollector( File crontab, Application application )
	{
		this.crontab = crontab;
		this.application = application;
	}

	//
	// TaskCollector
	//

	public TaskTable getTasks()
	{
		long lastModified = crontab.lastModified();

		if( lastModified == 0 )
		{
			// No crontab

			if( ( taskTable == null ) || ( taskTable.size() > 0 ) )
				taskTable = new TaskTable();

			return taskTable;
		}
		else if( lastModified <= lastParsed )
		{
			// No changes since we last parsed

			if( taskTable == null )
				taskTable = new TaskTable();

			return taskTable;
		}

		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( crontab ) );
			try
			{
				taskTable = new TaskTable();

				while( true )
				{
					String line = reader.readLine();
					if( line == null )
						break;

					line = line.trim();
					int length = line.length();

					// Skip empty lines and comments
					if( ( length == 0 ) || line.startsWith( "#" ) )
						continue;

					// Find the pattern
					SchedulingPattern pattern = null;
					int patternEnd = length;
					// In reverse, because pipes may allow multiple patterns
					for( ; patternEnd > 0; patternEnd-- )
					{
						String section = line.substring( 0, patternEnd );
						if( SchedulingPattern.validate( section ) )
						{
							pattern = new SchedulingPattern( section );
							break;
						}
					}

					if( pattern == null )
						continue;

					// Find the document name
					String documentName = line.substring( patternEnd ).trim();
					if( documentName.length() == 0 )
						continue;

					// Add the task
					taskTable.add( pattern, new ApplicationCronTask( application, documentName ) );
				}
			}
			catch( IOException x )
			{
				throw new RuntimeException( x );
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch( IOException x )
				{
					x.printStackTrace();
				}
			}
		}
		catch( FileNotFoundException x )
		{
			// No crontab
			if( ( taskTable == null ) || ( taskTable.size() > 0 ) )
				taskTable = new TaskTable();
		}

		return taskTable;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The crontab file.
	 */
	private final File crontab;

	/**
	 * The Restlet application in which tasks will execute.
	 */
	private final Application application;

	/**
	 * The task table.
	 */
	private TaskTable taskTable;

	/**
	 * Timestamp of last parsing of the crontab file.
	 */
	private long lastParsed;
}
