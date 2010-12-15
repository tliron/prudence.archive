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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.DelegatedHandlerConversationService;
import com.threecrickets.prudence.service.DelegatedHandlerDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * A general-purpose delegate used to enter defined entry points in a
 * Scripturian {@link Executable}. The entry points must be global functions,
 * closures, or whatever other technique the language engine uses to for entry
 * points.
 * <p>
 * A <code>conversation</code> service is sent as the first argument to all
 * entry points. Additionally, <code>document</code> and
 * <code>application</code> services are available as global services. See
 * {@link DelegatedHandlerConversationService},
 * {@link DelegatedHandlerDocumentService} and {@link ApplicationService}.
 * <p>
 * Note that the executable's output is sent to the system's standard output.
 * Most likely, you will not want to output anything from the executable.
 * However, this redirection is provided as a debugging convenience.
 * <p>
 * For a more sophisticated resource delegate, see
 * {@link DelegatedResourceHandler}.
 * <p>
 * Instances are not thread-safe.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.applicationServiceName</code>
 * : The name of the global variable with which to access the application
 * service. Defaults to "application". See {@link #getApplicationServiceName()}.
 * </li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.commonLibraryDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../../../libraries/". See {@link #getCommonLibraryDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.defaultLanguageTag:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultLanguageTag()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedHandler.defaultName:</code>
 * {@link String}, defaults to "default". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.documentServiceName</code>
 * : The name of the global variable with which to access the document service.
 * Defaults to "document". See {@link #getDocumentServiceName()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedHandler.errorWriter:</code>
 * {@link Writer}, defaults to standard error. See {@link #getErrorWriter()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.entryPointValidityCache:</code>
 * {@link ConcurrentMap}, default to a new {@link ConcurrentHashMap}. See
 * {@link #getEntryPointValidityCache(Executable)}.
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../uploads/". See {@link #getFileUploadDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold:</code>
 * {@link Integer}, defaults to zero. See {@link #getFileUploadSizeThreshold()}.
 * </li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.executionController:</code>
 * {@link ExecutionController}. See {@link #getExecutionController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.libraryDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../libraries/". See {@link #getLibraryDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance. See
 * {@link #getLanguageManager()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.prepare:</code>
 * {@link Boolean}, defaults to true. See {@link #isPrepare()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.writer:</code>
 * {@link Writer}, defaults to standard output. See {@link #getWriter()}.</li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class DelegatedHandler
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param documentName
	 *        The document name
	 * @param documentSource
	 *        The document source
	 * @param languageManager
	 *        The language manager
	 * @param context
	 *        The context used to configure the handler
	 */
	public DelegatedHandler( String documentName, DocumentSource<Executable> documentSource, LanguageManager languageManager, Context context )
	{
		this.documentSource = documentSource;
		this.documentName = documentName;
		this.languageManager = languageManager;
		this.context = context;
	}

	//
	// Attributes
	//

	/**
	 * @return The document source
	 */
	public DocumentSource<Executable> getDocumentSource()
	{
		return documentSource;
	}

	/**
	 * @return The document name
	 */
	public String getDocumentName()
	{
		return documentName;
	}

	/**
	 * The context used to configure the handler.
	 * 
	 * @return The context
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.languageManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			languageManager = (LanguageManager) attributes.get( "com.threecrickets.prudence.DelegatedHandler.languageManager" );

			if( languageManager == null )
			{
				languageManager = new LanguageManager();

				LanguageManager existing = (LanguageManager) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedHandler.languageManager", languageManager );
				if( existing != null )
					languageManager = existing;
			}
		}

		return languageManager;
	}

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.defaultLanguageTag</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag()
	{
		if( defaultLanguageTag == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			defaultLanguageTag = (String) attributes.get( "com.threecrickets.prudence.DelegatedHandler.defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "js";
		}

		return defaultLanguageTag;
	}

	/**
	 * Whether to prepare the executables. Preparation increases initialization
	 * time and reduces execution time. Note that not all languages support
	 * preparation as a separate operation. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.prepare</code> in the
	 * application's {@link Context}.
	 * 
	 * @return Whether to prepare executables
	 */
	public boolean isPrepare()
	{
		if( prepare == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			prepare = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedHandler.prepare" );

			if( prepare == null )
				prepare = true;
		}

		return prepare;
	}

	/**
	 * The {@link Writer} used by the {@link Executable}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.writer</code> in the
	 * application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public Writer getWriter()
	{
		if( writer == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			writer = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedHandler.writer" );

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
	 * <code>com.threecrickets.prudence.DelegatedHandler.errorWriter</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public Writer getErrorWriter()
	{
		if( errorWriter == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			errorWriter = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedHandler.errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.err );
		}

		return errorWriter;
	}

	/**
	 * Executables might use this directory for importing libraries. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../libraries/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.libraryDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public File getLibraryDirectory()
	{
		if( libraryDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			libraryDirectory = (File) attributes.get( "com.threecrickets.prudence.DelegatedHandler.libraryDirectory" );

			if( libraryDirectory == null )
			{
				if( documentSource instanceof DocumentFileSource<?> )
				{
					libraryDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../libraries/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedHandler.libraryDirectory", libraryDirectory );
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
	 * <code>com.threecrickets.prudence.DelegatedHandler.commonLibraryDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The common library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public File getCommonLibraryDirectory()
	{
		if( commonLibraryDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			commonLibraryDirectory = (File) attributes.get( "com.threecrickets.prudence.DelegatedHandler.commonLibraryDirectory" );

			if( commonLibraryDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					commonLibraryDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../../../libraries/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedHandler.commonLibraryDirectory", commonLibraryDirectory );
					if( existing != null )
						commonLibraryDirectory = existing;
				}
			}
		}

		return commonLibraryDirectory;
	}

	/**
	 * If the {@link #getDocumentSource()} is a {@link DocumentFileSource}, then
	 * this is the file relative to the {@link DocumentFileSource#getBasePath()}
	 * . Otherwise, it's null.
	 * 
	 * @return The relative library directory or null
	 */
	public File getRelativeFile( File file )
	{
		if( file != null )
		{
			DocumentSource<Executable> documentSource = getDocumentSource();
			if( documentSource instanceof DocumentFileSource<?> )
				return ScripturianUtil.getRelativeFile( file, ( (DocumentFileSource<?>) documentSource ).getBasePath() );
		}
		return null;
	}

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.documentServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			documentServiceName = (String) attributes.get( "com.threecrickets.prudence.DelegatedHandler.documentServiceName" );

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
	 * <code>com.threecrickets.prudence.DelegatedHandler.applicationServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			applicationServiceName = (String) attributes.get( "com.threecrickets.prudence.DelegatedHandler.applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	/**
	 * An optional {@link ExecutionController} to be used with the executable.
	 * Useful for exposing your own global variables to the executable.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.executionController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public ExecutionController getExecutionController()
	{
		if( executionController == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			executionController = (ExecutionController) attributes.get( "com.threecrickets.prudence.DelegatedHandler.executionController" );
		}

		return executionController;
	}

	/**
	 * The directory in which to place uploaded files. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../uploads/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.fileUploadDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The file upload directory or null
	 */
	public File getFileUploadDirectory()
	{
		if( fileUploadDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			fileUploadDirectory = (File) attributes.get( "com.threecrickets.prudence.DelegatedHandler.fileUploadDirectory" );

			if( fileUploadDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					fileUploadDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../uploads/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedHandler.fileUploadDirectory", fileUploadDirectory );
					if( existing != null )
						fileUploadDirectory = existing;
				}
			}
		}

		return fileUploadDirectory;
	}

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 * Defaults to zero, meaning that all uploaded files will be stored to disk.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedHandler.fileUploadSizeThreshold</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The file upload size threshold
	 */
	public int getFileUploadSizeThreshold()
	{
		if( fileUploadSizeThreshold == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			fileUploadSizeThreshold = ( (Number) attributes.get( "com.threecrickets.prudence.DelegatedHandler.fileUploadSizeThreshold" ) ).intValue();

			if( fileUploadSizeThreshold == null )
				fileUploadSizeThreshold = 0;
		}

		return fileUploadSizeThreshold;
	}

	/**
	 * A cache for entry point validity.
	 * 
	 * @param executable
	 *        The executable
	 * @return The entry point validity cache
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, Boolean> getEntryPointValidityCache( Executable executable )
	{
		ConcurrentMap<String, Object> attributes = executable.getAttributes();
		ConcurrentMap<String, Boolean> entryPointValidityCache = (ConcurrentMap<String, Boolean>) attributes.get( "com.threecrickets.prudence.DelegatedHandler.entryPointValidityCache" );
		if( entryPointValidityCache == null )
		{
			entryPointValidityCache = new ConcurrentHashMap<String, Boolean>();
			ConcurrentMap<String, Boolean> existing = (ConcurrentMap<String, Boolean>) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedHandler.entryPointValidityCache", entryPointValidityCache );
			if( existing != null )
				entryPointValidityCache = existing;
		}

		return entryPointValidityCache;
	}

	//
	// Operations
	//

	/**
	 * Enters the executable.
	 * 
	 * @param entryPointName
	 *        Name of entry point
	 * @param arguments
	 *        Extra arguments to add to entry point
	 * @return The result of the entry
	 * @throws ResourceException
	 * @see Executable#enter(String, Object...)
	 */
	public Object handle( String entryPointName, Object... arguments )
	{
		ConcurrentMap<String, Boolean> entryPointValidityCache = null;

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( getDocumentName(), getDocumentSource(), false, getLanguageManager(), getDefaultLanguageTag(), isPrepare() );
			Executable executable = documentDescriptor.getDocument();

			if( executable.getEnterableExecutionContext() == null )
			{
				ExecutionContext executionContext = new ExecutionContext( getWriter(), getErrorWriter() );

				// Add library locations
				File libraryDirectory = getLibraryDirectory();
				if( libraryDirectory != null )
					executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
				libraryDirectory = getCommonLibraryDirectory();
				if( libraryDirectory != null )
					executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

				executionContext.getServices().put( getDocumentServiceName(), new DelegatedHandlerDocumentService( this, getDocumentSource() ) );
				executionContext.getServices().put( getApplicationServiceName(), new ApplicationService() );

				try
				{
					if( !executable.makeEnterable( executionContext, this, getExecutionController() ) )
						executionContext.release();
				}
				catch( ParsingException x )
				{
					executionContext.release();
					throw new ResourceException( x );
				}
				catch( ExecutionException x )
				{
					executionContext.release();
					throw new ResourceException( x );
				}
				catch( IOException x )
				{
					executionContext.release();
					throw new ResourceException( x );
				}
			}

			// Check for validity, if cached
			entryPointValidityCache = getEntryPointValidityCache( executable );
			Boolean isValid = entryPointValidityCache.get( entryPointName );
			if( ( isValid != null ) && !isValid.booleanValue() )
				throw new NoSuchMethodException( entryPointName );

			// Enter!
			Object r;
			if( arguments != null )
			{
				ArrayList<Object> argumentList = new ArrayList<Object>( arguments.length + 1 );
				argumentList.add( new DelegatedHandlerConversationService( getFileUploadSizeThreshold(), getFileUploadDirectory() ) );
				for( Object argument : arguments )
					argumentList.add( argument );
				r = executable.enter( entryPointName, argumentList.toArray() );
			}
			else
				r = executable.enter( entryPointName, new DelegatedHandlerConversationService( getFileUploadSizeThreshold(), getFileUploadDirectory() ) );

			return r;
		}
		catch( DocumentNotFoundException x )
		{
			return null;
		}
		catch( DocumentException x )
		{
			throw new ResourceException( x );
		}
		catch( ParsingException x )
		{
			throw new ResourceException( x );
		}
		catch( ExecutionException x )
		{
			throw new ResourceException( x );
		}
		catch( NoSuchMethodException x )
		{
			// We are invalid
			if( entryPointValidityCache != null )
				entryPointValidityCache.put( entryPointName, false );

			return null;
		}
		finally
		{
			try
			{
				writer.flush();
				errorWriter.flush();
			}
			catch( IOException x )
			{
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document source.
	 */
	private final DocumentSource<Executable> documentSource;

	/**
	 * The document name.
	 */
	private final String documentName;

	/**
	 * The context used to configure the handler.
	 */
	private final Context context;

	/**
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	private LanguageManager languageManager;

	/**
	 * The default language tag to be used if the executable doesn't specify
	 * one.
	 */
	private String defaultLanguageTag;

	/**
	 * Whether to prepare executables.
	 */
	private Boolean prepare;

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	private Writer writer;

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private Writer errorWriter;

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
	 * The name of the global variable with which to access the document
	 * service.
	 */
	private String documentServiceName;

	/**
	 * The name of the global variable with which to access the application
	 * service.
	 */
	private String applicationServiceName;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	private ExecutionController executionController;

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	private Integer fileUploadSizeThreshold;

	/**
	 * The directory in which to place uploaded files.
	 */
	private File fileUploadDirectory;
}
