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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ApplicationTaskDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * @author Tal Liron
 */
public class ApplicationTask implements Runnable
{
	//
	// Construction
	//

	/**
	 * Construction using current application.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws ParsingException
	 * @throws DocumentException
	 * @see Application#getCurrent()
	 */
	public ApplicationTask( String documentName ) throws ParsingException, DocumentException
	{
		this( Application.getCurrent(), documentName );
	}

	/**
	 * Construction.
	 * 
	 * @param application
	 *        The application
	 * @param documentName
	 *        The document name
	 * @throws DocumentException
	 * @throws ParsingException
	 */
	public ApplicationTask( Application application, String documentName ) throws ParsingException, DocumentException
	{
		this.application = application;

		executable = Executable.createOnce( documentName, getDocumentSource(), false, getLanguageManager(), getDefaultLanguageTag(), isPrepare() ).getDocument();
	}

	//
	// Attributes
	//

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
	 * one. Defaults to "js".
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
				defaultLanguageTag = "js";
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
	 * If the {@link #getDocumentSource()} is a {@link DocumentFileSource}, then
	 * this is the library directory relative to the
	 * {@link DocumentFileSource#getBasePath()}. Otherwise, it's null.
	 * 
	 * @return The relative library directory or null
	 */
	public File getLibraryDirectoryRelative()
	{
		DocumentSource<Executable> documentSource = getDocumentSource();
		if( documentSource instanceof DocumentFileSource<?> )
		{
			File libraryDirectory = getLibraryDirectory();
			if( libraryDirectory != null )
				return ScripturianUtil.getRelativeFile( libraryDirectory, ( (DocumentFileSource<?>) documentSource ).getBasePath() );
		}
		return null;
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

	public File getTaskDirectoryRelative()
	{
		// TODO Auto-generated method stub
		return null;
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

			ExecutionContext executionContext = new ExecutionContext();

			File libraryDirectory = getLibraryDirectory();
			if( libraryDirectory != null )
				executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

			executionContext.getServices().put( getDocumentServiceName(), new ApplicationTaskDocumentService( this, getDocumentSource() ) );
			executionContext.getServices().put( getApplicationServiceName(), new ApplicationService( application ) );

			try
			{
				executable.execute( executionContext );
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
	 * The executable.
	 */
	private final Executable executable;

	/**
	 * The application.
	 */
	private final Application application;

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