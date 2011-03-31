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

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;

import com.threecrickets.prudence.internal.attributes.ApplicationTaskAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ApplicationTaskDocumentService;
import com.threecrickets.prudence.service.DocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * A {@link Runnable} wrapper for a Scripturian {@link Executable}.
 * <p>
 * <code>document</code> and <code>application</code> services are available as
 * global variables. See {@link DocumentService} and {@link ApplicationService}.
 * <p>
 * Before using this class, make sure to configure a valid document source in
 * the application's {@link Context} as
 * <code>com.threecrickets.prudence.ApplicationTask.documentSource</code>. This
 * document source is exposed to the executable as <code>document.source</code>.
 * <p>
 * Instances are not thread-safe.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.applicationServiceName</code>
 * : Defaults to "application".</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.commonLibraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript".</li>
 * <li><code>com.threecrickets.prudence.ApplicationTask.defaultName:</code>
 * {@link String}, defaults to "default".</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.documentServiceName</code> :
 * The name of the global variable with which to access the document service.
 * Defaults to "document".</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b></li>
 * <li><code>com.threecrickets.prudence.ApplicationTask.errorWriter:</code>
 * {@link Writer}, defaults to standard error.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.executionController:</code>
 * {@link ExecutionController}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.libraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.prepare:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.ApplicationTask.writer:</code>
 * {@link Writer}, defaults to standard output.</li>
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
	 * Constructor using current Restlet application.
	 * 
	 * @param documentName
	 *        The document name
	 * @param context
	 *        The context made available to the task
	 * @see Application#getCurrent()
	 */
	public ApplicationTask( String documentName, Object context )
	{
		this( Application.getCurrent(), documentName, context );
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The Restlet application in which this task will execute
	 * @param documentName
	 *        The document name
	 * @param context
	 *        The context made available to the task
	 */
	public ApplicationTask( Application application, String documentName, Object context )
	{
		attributes = new ApplicationTaskAttributes( application );
		this.application = application;
		this.documentName = documentName;
		this.context = context;
	}

	//
	// Attributes
	//

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

	/**
	 * The context made available to the task.
	 * 
	 * @return The context
	 */
	public Object getContext()
	{
		return context;
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
				DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, false, attributes.createParsingContext() );
				Executable executable = documentDescriptor.getDocument();

				ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
				attributes.addLibraryLocations( executionContext );

				executionContext.getServices().put( attributes.getDocumentServiceName(), new ApplicationTaskDocumentService( attributes, documentDescriptor, context ) );
				executionContext.getServices().put( attributes.getApplicationServiceName(), new ApplicationService( application ) );

				executable.execute( executionContext, this, attributes.getExecutionController() );
			}
			catch( DocumentNotFoundException x )
			{
				application.getLogger().warning( "Task not found: " + documentName );
				throw new RuntimeException( x );
			}
			catch( DocumentException x )
			{
				application.getLogger().log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( ParsingException x )
			{
				application.getLogger().log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( ExecutionException x )
			{
				application.getLogger().log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( IOException x )
			{
				application.getLogger().log( Level.SEVERE, "Exception or error caught in task", x );
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

	/**
	 * The context made available to the task.
	 */
	private final Object context;
}
