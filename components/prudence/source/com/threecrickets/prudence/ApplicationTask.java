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

package com.threecrickets.prudence;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.internal.attributes.ApplicationTaskAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ApplicationTaskDocumentService;
import com.threecrickets.prudence.service.DocumentService;
import com.threecrickets.prudence.util.LoggingUtil;
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
 * @param <T>
 *        The return type
 */
public class ApplicationTask<T> implements Callable<T>, Runnable
{
	//
	// Construction
	//

	/**
	 * Constructor using current Restlet application.
	 * 
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task
	 * @see Application#getCurrent()
	 */
	public ApplicationTask( String documentName, String entryPointName, Object context )
	{
		this( Application.getCurrent(), documentName, entryPointName, context );
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The Restlet application in which this task will execute
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task
	 */
	public ApplicationTask( Application application, String documentName, String entryPointName, Object context )
	{
		attributes = new ApplicationTaskAttributes( application );
		this.application = application;
		this.documentName = documentName;
		this.entryPointName = entryPointName;
		this.context = context;
		code = null;
	}

	/**
	 * Constructor using current Restlet application.
	 * 
	 * @param code
	 *        The code to execute
	 * @param context
	 *        The context made available to the task
	 */
	public ApplicationTask( String code, Object context )
	{
		this( Application.getCurrent(), code, context );
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The Restlet application in which this task will execute
	 * @param code
	 *        The code to execute
	 * @param context
	 *        The context made available to the task
	 */
	public ApplicationTask( Application application, String code, Object context )
	{
		attributes = new ApplicationTaskAttributes( application );
		this.application = application;
		this.code = code;
		this.context = context;
		documentName = getOnTheFlyName( code );
		entryPointName = null;
	}

	//
	// Attributes
	//

	/**
	 * The attributes as configured in the {@link Application} context.
	 * 
	 * @return The attributes
	 */
	public ApplicationTaskAttributes getAttributes()
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

	/**
	 * The entry point name.
	 * 
	 * @return The entry point name or null
	 */
	public String getEntryPointName()
	{
		return entryPointName;
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
	// Callable
	//

	public T call()
	{
		Application oldApplication = Application.getCurrent();
		try
		{
			Application.setCurrent( application );

			ConcurrentMap<String, Boolean> entryPointValidityCache = null;

			try
			{
				DocumentDescriptor<Executable> documentDescriptor;
				if( code != null )
					documentDescriptor = attributes.createDocumentOnce( documentName, code );
				else
					documentDescriptor = attributes.createDocumentOnce( documentName, false, true, true, false );

				Executable executable = documentDescriptor.getDocument();

				if( entryPointName == null )
				{
					// Simple execution

					ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
					attributes.addLibraryLocations( executionContext );

					executionContext.getServices().put( attributes.getDocumentServiceName(), new ApplicationTaskDocumentService( attributes, documentDescriptor, context ) );
					executionContext.getServices().put( attributes.getApplicationServiceName(), ApplicationService.create( application ) );

					executable.execute( executionContext, this, attributes.getExecutionController() );
				}
				else
				{
					// Enter

					Object enteringKey = application.hashCode();

					if( executable.getEnterableExecutionContext( enteringKey ) == null )
					{
						ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
						attributes.addLibraryLocations( executionContext );

						executionContext.getServices().put( attributes.getDocumentServiceName(), new DocumentService<ApplicationTaskAttributes>( attributes, documentDescriptor ) );
						executionContext.getServices().put( attributes.getApplicationServiceName(), ApplicationService.create( application ) );

						try
						{
							if( !executable.makeEnterable( enteringKey, executionContext, this, attributes.getExecutionController() ) )
								executionContext.release();
						}
						catch( ParsingException x )
						{
							LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
							executionContext.release();
							throw new ResourceException( x );
						}
						catch( ExecutionException x )
						{
							LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
							executionContext.release();
							throw new ResourceException( x );
						}
						catch( IOException x )
						{
							LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
							executionContext.release();
							throw new ResourceException( x );
						}
					}

					// Check for validity, if cached
					entryPointValidityCache = attributes.getEntryPointValidityCache( executable );
					Boolean isValid = entryPointValidityCache.get( entryPointName );
					if( ( isValid != null ) && !isValid.booleanValue() )
						throw new NoSuchMethodException( entryPointName );

					// Enter!
					@SuppressWarnings("unchecked")
					T r = (T) executable.enter( enteringKey, entryPointName, context );

					return r;
				}
			}
			catch( DocumentNotFoundException x )
			{
				LoggingUtil.getLogger( application ).warning( "Task not found: " + documentName );
				throw new RuntimeException( x );
			}
			catch( DocumentException x )
			{
				LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( ParsingException x )
			{
				LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( ExecutionException x )
			{
				LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( IOException x )
			{
				LoggingUtil.getLogger( application ).log( Level.SEVERE, "Exception or error caught in task", x );
				throw new RuntimeException( x );
			}
			catch( NoSuchMethodException x )
			{
				// We are invalid
				if( entryPointValidityCache != null )
					entryPointValidityCache.put( entryPointName, false );
			}
		}
		finally
		{
			Application.setCurrent( oldApplication );
		}

		return null;
	}

	//
	// Runnable
	//

	public void run()
	{
		call();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The attributes as configured in the {@link Application} context.
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
	 * The entry point name.
	 */
	private final String entryPointName;

	/**
	 * The context made available to the task.
	 */
	private final Object context;

	/**
	 * The code to execute.
	 */
	private final String code;

	/**
	 * Cache for on-the-fly names.
	 */
	private static final ConcurrentHashMap<String, String> onTheFlyNames = new ConcurrentHashMap<String, String>();

	private static String getOnTheFlyName( String code )
	{
		String name = onTheFlyNames.get( code );
		if( name == null )
		{
			name = Executable.createOnTheFlyName();
			String existing = onTheFlyNames.putIfAbsent( code, name );
			if( existing != null )
				name = existing;
		}
		return name;
	}
}
