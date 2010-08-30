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
 * For a more sophisticated resource delegate, see {@link DelegatedResource}.
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
	 */
	public DelegatedHandler( String documentName, DocumentSource<Executable> documentSource, LanguageManager languageManager )
	{
		this.documentSource = documentSource;
		this.documentName = documentName;
		this.languageManager = languageManager;

		if( documentSource instanceof DocumentFileSource<?> )
		{
			DocumentFileSource<?> documentFileSource = (DocumentFileSource<?>) documentSource;
			libraryDirectory = new File( documentFileSource.getBasePath(), "../libraries/" );
			fileUploadDirectory = new File( documentFileSource.getBasePath(), "../uploads/" );
		}
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
	 * @return The documentName
	 */
	public String getDocumentName()
	{
		return documentName;
	}

	/**
	 * @return The languageManager
	 */
	public LanguageManager getLanguageManager()
	{
		return languageManager;
	}

	/**
	 * Defaults to "javascript".
	 * 
	 * @return The default language tag
	 * @see #setDefaultLanguageTag(String)
	 */
	public String getDefaultLanguageTag()
	{
		return defaultLanguageTag;
	}

	/**
	 * @param defaultLanguageTag
	 *        The default language tag
	 * @see #getDefaultLanguageTag()
	 */
	public void setDefaultLanguageTag( String defaultLanguageTag )
	{
		this.defaultLanguageTag = defaultLanguageTag;
	}

	/**
	 * Defaults to true.
	 * 
	 * @return Whether to prepare executables
	 * @see #setPrepare(boolean)
	 */
	public boolean isPrepare()
	{
		return prepare;
	}

	/**
	 * @param prepare
	 *        Whether to prepare executables
	 * @see #isPrepare()
	 */
	public void setPrepare( boolean prepare )
	{
		this.prepare = prepare;
	}

	/**
	 * Defaults to standard output.
	 * 
	 * @return The writer
	 * @see #setWriter(Writer)
	 */
	public Writer getWriter()
	{
		return writer;
	}

	/**
	 * @param writer
	 *        The writer
	 * @see #getWriter()
	 */
	public void setWriter( Writer writer )
	{
		this.writer = writer;
	}

	/**
	 * Defaults to standard error.
	 * 
	 * @return The error writer
	 * @see #setErrorWriter(Writer)
	 */
	public Writer getErrorWriter()
	{
		return errorWriter;
	}

	/**
	 * @param errorWriter
	 *        The error writer
	 * @see #getErrorWriter()
	 */
	public void setErrorWriter( Writer errorWriter )
	{
		this.errorWriter = errorWriter;
	}

	/**
	 * Defaults to the {@link DocumentFileSource#getBasePath()} plus
	 * "../library/".
	 * 
	 * @return The library directory
	 * @see #setLibraryDirectory(File)
	 */
	public File getLibraryDirectory()
	{
		return libraryDirectory;
	}

	/**
	 * @param libraryDirectory
	 *        The library directory
	 * @see #getLibraryDirectory()
	 */
	public void setLibraryDirectory( File libraryDirectory )
	{
		this.libraryDirectory = libraryDirectory;
	}

	/**
	 * Defaults to "document".
	 * 
	 * @return The document service name
	 * @see #setDocumentServiceName(String)
	 */
	public String getDocumentServiceName()
	{
		return documentServiceName;
	}

	/**
	 * @param documentServiceName
	 *        The document service name
	 * @see #getDocumentServiceName()
	 */
	public void setDocumentServiceName( String documentServiceName )
	{
		this.documentServiceName = documentServiceName;
	}

	/**
	 * Defaults to "application".
	 * 
	 * @return The application service name
	 * @see #setApplicationServiceName(String)
	 */
	public String getApplicationServiceName()
	{
		return applicationServiceName;
	}

	/**
	 * @param applicationServiceName
	 *        The application service name
	 * @see #getApplicationServiceName()
	 */
	public void setApplicationServiceName( String applicationServiceName )
	{
		this.applicationServiceName = applicationServiceName;
	}

	/**
	 * Defaults to null.
	 * 
	 * @return The execution controller
	 * @see #setExecutionController(ExecutionController)
	 */
	public ExecutionController getExecutionController()
	{
		return executionController;
	}

	/**
	 * @param executionController
	 *        The execution controller
	 * @see #getExecutionController()
	 */
	public void setExecutionController( ExecutionController executionController )
	{
		this.executionController = executionController;
	}

	/**
	 * Defaults to zero.
	 * 
	 * @return The file upload size threshold
	 * @see #setFileUploadSizeThreshold(int)
	 */
	public int getFileUploadSizeThreshold()
	{
		return fileUploadSizeThreshold;
	}

	/**
	 * @param fileUploadSizeThreshold
	 *        The file upload size threshold
	 * @see #getFileUploadSizeThreshold()
	 */
	public void setFileUploadSizeThreshold( int fileUploadSizeThreshold )
	{
		this.fileUploadSizeThreshold = fileUploadSizeThreshold;
	}

	/**
	 * Defaults to the {@link DocumentFileSource#getBasePath()} plus
	 * "../uploads/".
	 * 
	 * @return The file upload directory
	 * @see #setFileUploadDirectory(File)
	 */
	public File getFileUploadDirectory()
	{
		return fileUploadDirectory;
	}

	/**
	 * @param fileUploadDirectory
	 *        The file upload directory
	 * @see #getFileUploadDirectory()
	 */
	public void setFileUploadDirectory( File fileUploadDirectory )
	{
		this.fileUploadDirectory = fileUploadDirectory;
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
	 * @see {@link Executable#enter(String, Object...)}
	 */
	public Object handle( String entryPointName, Object... arguments )
	{
		ConcurrentMap<String, Boolean> entryPointValidityCache = null;

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, documentSource, false, languageManager, defaultLanguageTag, prepare );
			Executable executable = documentDescriptor.getDocument();

			if( executable.getEnterableExecutionContext() == null )
			{
				ExecutionContext executionContext = new ExecutionContext( writer, errorWriter );

				if( libraryDirectory != null )
					executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

				executionContext.getServices().put( documentServiceName, new DelegatedHandlerDocumentService( documentSource ) );
				executionContext.getServices().put( applicationServiceName, new ApplicationService() );

				try
				{
					if( !executable.makeEnterable( executionContext, this, executionController ) )
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
				argumentList.add( new DelegatedHandlerConversationService( fileUploadSizeThreshold, fileUploadDirectory ) );
				for( Object argument : arguments )
					argumentList.add( argument );
				r = executable.enter( entryPointName, argumentList.toArray() );
			}
			else
				r = executable.enter( entryPointName, new DelegatedHandlerConversationService( fileUploadSizeThreshold, fileUploadDirectory ) );

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
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	private final LanguageManager languageManager;

	/**
	 * The default language tag to be used if the executable doesn't specify
	 * one.
	 */
	private volatile String defaultLanguageTag = "javascript";

	/**
	 * Whether to prepare executables.
	 */
	private volatile boolean prepare = true;

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	private Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private volatile Writer errorWriter = new OutputStreamWriter( System.err );

	/**
	 * Executables might use directory this for importing libraries.
	 */
	private volatile File libraryDirectory;

	/**
	 * The name of the global variable with which to access the document
	 * service.
	 */
	private volatile String documentServiceName = "document";

	/**
	 * The name of the global variable with which to access the application
	 * service.
	 */
	private volatile String applicationServiceName = "application";

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	private volatile ExecutionController executionController;

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	private volatile int fileUploadSizeThreshold = 0;

	/**
	 * The directory in which to place uploaded files.
	 */
	private volatile File fileUploadDirectory;

	/**
	 * A cache for entry point validity.
	 * 
	 * @param executable
	 *        The executable
	 * @return The entry point validity cache
	 */
	@SuppressWarnings("unchecked")
	private ConcurrentMap<String, Boolean> getEntryPointValidityCache( Executable executable )
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
}
