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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

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
		this.application = application;
		this.documentName = documentName;
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
	 * The {@link Writer} used by the {@link Executable}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.writer</code> in the
	 * application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public Writer getWriter()
	{
		if( writer == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			writer = (Writer) attributes.get( "com.threecrickets.prudence.ApplicationTask.writer" );

			if( writer == null )
				writer = new OutputStreamWriter( System.out );
		}

		return writer;
	}

	/**
	 * Same as {@link #getWriter()}, for standard error. Defaults to standard
	 * error.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.errorWriter</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public Writer getErrorWriter()
	{
		if( errorWriter == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			errorWriter = (Writer) attributes.get( "com.threecrickets.prudence.ApplicationTask.errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.err );
		}

		return errorWriter;
	}

	/**
	 * An optional {@link ExecutionController} to be used with the executable.
	 * Useful for exposing your own global variables to the executable.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.executionController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public ExecutionController getExecutionController()
	{
		if( executionController == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			executionController = (ExecutionController) attributes.get( "com.threecrickets.prudence.ApplicationTask.executionController" );
		}

		return executionController;
	}

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.documentServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			documentServiceName = (String) attributes.get( "com.threecrickets.prudence.ApplicationTask.documentServiceName" );

			if( documentServiceName == null )
				documentServiceName = "document";
		}

		return documentServiceName;
	}

	/**
	 * The name of the global variable with which to access the application
	 * service. Defaults to "application".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.applicationServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			applicationServiceName = (String) attributes.get( "com.threecrickets.prudence.ApplicationTask.applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "javascript".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.defaultLanguageTag</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag()
	{
		if( defaultLanguageTag == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			defaultLanguageTag = (String) attributes.get( "com.threecrickets.prudence.ApplicationTask.defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "javascript";
		}

		return defaultLanguageTag;
	}

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "default".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.defaultName</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			defaultName = (String) attributes.get( "com.threecrickets.prudence.ApplicationTask.defaultName" );

			if( defaultName == null )
				defaultName = "default";
		}

		return defaultName;
	}

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.languageManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			languageManager = (LanguageManager) attributes.get( "com.threecrickets.prudence.ApplicationTask.languageManager" );

			if( languageManager == null )
			{
				languageManager = new LanguageManager();

				LanguageManager existing = (LanguageManager) attributes.putIfAbsent( "com.threecrickets.prudence.ApplicationTask.languageManager", languageManager );
				if( existing != null )
					languageManager = existing;
			}
		}

		return languageManager;
	}

	/**
	 * The {@link DocumentSource} used to fetch and cache documents. This must
	 * be set to a valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.documentSource</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getDocumentSource()
	{
		if( documentSource == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			documentSource = (DocumentSource<Executable>) attributes.get( "com.threecrickets.prudence.ApplicationTask.documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.ApplicationTask.documentSource must be set in context to use ApplicationTask" );
		}

		return documentSource;
	}

	/**
	 * Executables might use this directory for importing libraries. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../libraries/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.libraryDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public File getLibraryDirectory()
	{
		if( libraryDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			libraryDirectory = (File) attributes.get( "com.threecrickets.prudence.ApplicationTask.libraryDirectory" );

			if( libraryDirectory == null )
			{
				if( documentSource instanceof DocumentFileSource<?> )
				{
					libraryDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../libraries/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.ApplicationTask.libraryDirectory", libraryDirectory );
					if( existing != null )
						libraryDirectory = existing;
				}
			}
		}

		return libraryDirectory;
	}

	/**
	 * Executables from all applications might use this directory for importing
	 * libraries. If the {@link #getDocumentSource()} is a
	 * {@link DocumentFileSource}, then this will default to the
	 * {@link DocumentFileSource#getBasePath()} plus "../../../libraries/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.commonLibraryDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The common library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public File getCommonLibraryDirectory()
	{
		if( commonLibraryDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			commonLibraryDirectory = (File) attributes.get( "com.threecrickets.prudence.ApplicationTask.commonLibraryDirectory" );

			if( commonLibraryDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					commonLibraryDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../../../libraries/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.ApplicationTask.commonLibraryDirectory", commonLibraryDirectory );
					if( existing != null )
						commonLibraryDirectory = existing;
				}
			}
		}

		return commonLibraryDirectory;
	}

	/**
	 * Whether or not trailing slashes are required. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.trailingSlashRequired</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isTrailingSlashRequired()
	{
		if( trailingSlashRequired == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			trailingSlashRequired = (Boolean) attributes.get( "com.threecrickets.prudence.ApplicationTask.trailingSlashRequired" );

			if( trailingSlashRequired == null )
				trailingSlashRequired = true;
		}

		return trailingSlashRequired;
	}

	/**
	 * Whether to prepare the executables. Preparation increases initialization
	 * time and reduces execution time. Note that not all languages support
	 * preparation as a separate operation. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ApplicationTask.prepare</code> in the
	 * application's {@link Context}.
	 * 
	 * @return Whether to prepare executables
	 */
	public boolean isPrepare()
	{
		if( prepare == null )
		{
			ConcurrentMap<String, Object> attributes = application.getContext().getAttributes();
			prepare = (Boolean) attributes.get( "com.threecrickets.prudence.ApplicationTask.prepare" );

			if( prepare == null )
				prepare = true;
		}

		return prepare;
	}

	//
	// Operations
	//

	/**
	 * Throws an exception if the document name is not valid. Uses
	 * {@link #getDefaultName()} if no name is given, and respect
	 * {@link #isTrailingSlashRequired()}.
	 * 
	 * @param documentName
	 *        The document name
	 * @return The valid document name
	 * @throws ResourceException
	 */
	public String validateDocumentName( String documentName ) throws ResourceException
	{
		if( isTrailingSlashRequired() )
			if( ( documentName != null ) && ( documentName.length() != 0 ) && !documentName.endsWith( "/" ) )
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

		if( ( documentName == null ) || ( documentName.length() == 0 ) || ( documentName.equals( "/" ) ) )
		{
			documentName = getDefaultName();
			if( isTrailingSlashRequired() && !documentName.endsWith( "/" ) )
				documentName += "/";
		}

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
				Executable executable = Executable.createOnce( documentName, getDocumentSource(), false, getLanguageManager(), getDefaultLanguageTag(), isPrepare() ).getDocument();

				ExecutionContext executionContext = new ExecutionContext( getWriter(), getErrorWriter() );

				// Add library locations
				File libraryDirectory = getLibraryDirectory();
				if( libraryDirectory != null )
					executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
				libraryDirectory = getCommonLibraryDirectory();
				if( libraryDirectory != null )
					executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

				executionContext.getServices().put( getDocumentServiceName(), new ApplicationTaskDocumentService( this, getDocumentSource() ) );
				executionContext.getServices().put( getApplicationServiceName(), new ApplicationService( application ) );

				executable.execute( executionContext, this, getExecutionController() );
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
	 * The Restlet application in which this task will execute.
	 */
	private final Application application;

	/**
	 * The document name.
	 */
	private final String documentName;

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	private Writer writer;

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private Writer errorWriter;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	private ExecutionController executionController;

	/**
	 * The document service name.
	 */
	private String documentServiceName;

	/**
	 * The application service name.
	 */
	private String applicationServiceName;

	/**
	 * The default language tag to be used if the executable doesn't specify
	 * one.
	 */
	private String defaultLanguageTag;

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private String defaultName;

	/**
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	private LanguageManager languageManager;

	/**
	 * Executables might use directory this for importing libraries.
	 */
	private File libraryDirectory;

	/**
	 * Executables from all applications might use this directory for importing
	 * libraries.
	 */
	private File commonLibraryDirectory;

	/**
	 * Whether or not trailing slashes are required for all requests.
	 */
	private Boolean trailingSlashRequired;

	/**
	 * Whether to prepare executables.
	 */
	private Boolean prepare;

	/**
	 * The {@link DocumentSource} used to fetch scripts.
	 */
	private DocumentSource<Executable> documentSource;
}