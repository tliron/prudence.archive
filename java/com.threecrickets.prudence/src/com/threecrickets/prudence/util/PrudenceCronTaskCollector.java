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

package com.threecrickets.prudence.util;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.restlet.Context;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public class PrudenceCronTaskCollector implements TaskCollector
{
	//
	// Construction
	//

	public PrudenceCronTaskCollector( File crontab, DocumentSource<Executable> documentSource, LanguageManager languageManager, String defaultLanguageTag, boolean prepare, Context context )
	{
		this.crontab = crontab;
		this.documentSource = documentSource;
		this.languageManager = languageManager;
		this.defaultLanguageTag = defaultLanguageTag;
		this.prepare = prepare;
		this.context = context;
	}

	//
	// TaskCollector
	//

	public TaskTable getTasks()
	{
		TaskTable taskTable = new TaskTable();

		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( crontab ) );
			try
			{
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
					int patternEnd = 0;
					for( ; patternEnd < length; patternEnd++ )
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
					Executable executable = Executable.createOnce( documentName, documentSource, false, languageManager, defaultLanguageTag, prepare ).getDocument();
					taskTable.add( pattern, new PrudenceCronTask( executable ) );
				}
			}
			catch( IOException x )
			{
				x.printStackTrace();
			}
			catch( ParsingException x )
			{
				x.printStackTrace();
			}
			catch( DocumentException x )
			{
				x.printStackTrace();
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
			// No crontab -- that's alright!
		}

		return taskTable;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File crontab;

	private final DocumentSource<Executable> documentSource;

	private final LanguageManager languageManager;

	private final String defaultLanguageTag;

	private final boolean prepare;

	private final Context context;
}
