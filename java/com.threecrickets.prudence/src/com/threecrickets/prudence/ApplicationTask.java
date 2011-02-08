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

package com.threecrickets.prudence;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.restlet.Application;
import org.restlet.Context;

import com.threecrickets.prudence.internal.ApplicationTaskAttributes;
import com.threecrickets.prudence.internal.ContextualAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ApplicationTaskDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * A {@link Runnable} wrapper for a Scripturian {@link Executable}.
 * <p>
 * <code>document</code> and <code>application</code> services are available as
 * global variables. See {@link ApplicationTaskDocumentService} and
 * {@link ApplicationService}.
 * <p>
 * Before using this class, make sure to configure a valid document source in
 * the application's {@link Context}; see {@link #getDocumentSource()}. This
 * document source is exposed to the executable as <code>document.source</code>.
 * <p>
 * Instances are not thread-safe.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.applicationServiceName</code>
 * : The name of the global variable with which to access the application
 * service. Defaults to "application". See {@link #getApplicationServiceName()}.
 * </li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.commonLibraryDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../../../libraries/". See {@link #getCommonLibraryDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript". See
 * {@link #getDefaultLanguageTag()}.</li>
 * <li><code>com.threecrickets.prudence.ApplicationTask.defaultName:</code>
 * {@link String}, defaults to "default". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.documentServiceName</code> :
 * The name of the global variable with which to access the document service.
 * Defaults to "document". See {@link #getDocumentServiceName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b> See {@link #getDocumentSource()}.</li>
 * <li><code>com.threecrickets.prudence.ApplicationTask.errorWriter:</code>
 * {@link Writer}, defaults to standard error. See {@link #getErrorWriter()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.executionController:</code>
 * {@link ExecutionController}. See {@link #getExecutionController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.libraryDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../libraries/". See {@link #getLibraryDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance. See
 * {@link #getLanguageManager()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.prepare:</code>
 * {@link Boolean}, defaults to true. See {@link #isPrepare()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true. See {@link #isTrailingSlashRequired()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.writer:</code>
 * {@link Writer}, defaults to standard output. See {@link #getWriter()}.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 */
public class ApplicationTask implements Runnable
{
	//
	// Construction
	//

	/**
	 * Construction using current Restlet application.
	 * 
	 * @param documentName
	 *        The document name
	 * @see Application#getCurrent()
	 */
	public ApplicationTask( String documentName )
	{
		this( Application.getCurrent(), documentName );
	}

	/**
	 * Construction.
	 * 
	 * @param application
	 *        The Restlet application in which this task will execute
	 * @param documentName
	 *        The document name
	 */
	public ApplicationTask( Application application, String documentName )
	{
		attributes = new ApplicationTaskAttributes( application );
		this.application = application;
		this.documentName = documentName;
	}

	//
	// Attributes
	//

	/**
	 * The attributes.
	 * 
	 * @return The attributes
	 */
	public ContextualAttributes getAttributes()
	{
		return attributes;
	}

	/**
	 * The Restlet application in which this task will execute.
	 * 
	 * @return The application
	 */
	public Application getApplication()
	{
		return application;
	}

	/**
	 * The document name to execute for this task.
	 * 
	 * @return The document name
	 */
	public String getDocumentName()
	{
		return documentName;
	}

	//
	// Runnable
	//

	public void run()
	{
		Application oldApplication = Application.getCurrent();
		try
		{
			Application.setCurrent( application );

			try
			{
				Executable executable = Executable.createOnce( documentName, attributes.getDocumentSource(), false, attributes.getLanguageManager(), attributes.getDefaultLanguageTag(), attributes.isPrepare() )
					.getDocument();

				ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
				attributes.addLibraryLocations( executionContext );

				executionContext.getServices().put( attributes.getDocumentServiceName(), new ApplicationTaskDocumentService( this ) );
				executionContext.getServices().put( attributes.getApplicationServiceName(), new ApplicationService( application ) );

				executable.execute( executionContext, this, attributes.getExecutionController() );
			}
			catch( DocumentException x )
			{
				throw new RuntimeException( x );
			}
			catch( ParsingException x )
			{
				throw new RuntimeException( x );
			}
			catch( ExecutionException x )
			{
				throw new RuntimeException( x );
			}
			catch( IOException x )
			{
				throw new RuntimeException( x );
			}
		}
		finally
		{
			Application.setCurrent( oldApplication );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The attributes.
	 */
	private final ApplicationTaskAttributes attributes;

	/**
	 * The Restlet application in which this task will execute.
	 */
	private final Application application;

	/**
	 * The document name.
	 */
	private final String documentName;
}
