package com.threecrickets.prudence.internal;

import java.io.File;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;

public abstract class ContextualAttributes implements DocumentExecutionAttributes
{
	//
	// Construction
	//
	
	public ContextualAttributes( String prefix )
	{
		this.prefix = prefix;
	}

	/**
	 * The contextual attributes.
	 * 
	 * @return The attributes
	 */
	public abstract ConcurrentMap<String, Object> getAttributes();

	/**
	 * The {@link Writer} used by the {@link Executable}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>writer</code> in the application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public abstract Writer getWriter();

	/**
	 * Same as {@link #getWriter()}, for standard error. Defaults to standard
	 * error.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>errorWriter</code> in the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public abstract Writer getErrorWriter();

	/**
	 * The {@link DocumentSource} used to fetch documents. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentSource</code> in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	public abstract DocumentSource<Executable> getDocumentSource();

	/**
	 * Executables might use this directory for importing libraries. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../libraries/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>libraryDirectory</code> in the application's {@link Context}.
	 * 
	 * @return The library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public abstract DocumentSource<Executable> getLibrariesDocumentSource();

	/**
	 * Executables from all applications might use this directory for importing
	 * libraries. If the {@link #getDocumentSource()} is a
	 * {@link DocumentFileSource}, then this will default to the
	 * {@link DocumentFileSource#getBasePath()} plus "../../../libraries/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>commonLibraryDirectory</code> in the application's {@link Context}.
	 * 
	 * @return The common library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public abstract DocumentSource<Executable> getCommonLibrariesDocumentSource();

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "default".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultName</code> in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public abstract String getDefaultName();

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "javascript".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultLanguageTag</code> in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public abstract String getDefaultLanguageTag();

	/**
	 * Whether or not trailing slashes are required. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>trailingSlashRequired</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public abstract boolean isTrailingSlashRequired();

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public abstract String getDocumentServiceName();

	/**
	 * The name of the global variable with which to access the application
	 * service. Defaults to "application".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>applicationServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public abstract String getApplicationServiceName();

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>languageManager</code> in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public abstract LanguageManager getLanguageManager();

	/**
	 * Whether to prepare the executables. Preparation increases initialization
	 * time and reduces execution time. Note that not all languages support
	 * preparation as a separate operation. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>prepare</code> in the application's {@link Context}.
	 * 
	 * @return Whether to prepare executables
	 */
	public abstract boolean isPrepare();

	/**
	 * An optional {@link ExecutionController} to be used with the executable.
	 * Useful for exposing your own global variables to the executable.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>executionController</code> in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public abstract ExecutionController getExecutionController();

	/**
	 * This is so we can see the source code for documents by adding
	 * <code>?source=true</code> to the URL. You probably wouldn't want this for
	 * most applications. Defaults to false.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>sourceViewable</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of source code
	 */
	public abstract boolean isSourceViewable();

	/**
	 * An optional {@link DocumentFormatter} to use for representing source
	 * code. Defaults to a {@link JygmentsDocumentFormatter}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentFormatter</code> in the application's {@link Context}.
	 * 
	 * @return The document formatter or null
	 * @see #isSourceViewable()
	 */
	public abstract DocumentFormatter<Executable> getDocumentFormatter();

	/**
	 * The default character set to be used if the client does not specify it.
	 * Defaults to {@link CharacterSet#UTF_8}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultCharacterSet</code> in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public abstract CharacterSet getDefaultCharacterSet();

	/**
	 * The directory in which to place uploaded files. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../uploads/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>fileUploadDirectory</code> in the application's {@link Context}.
	 * 
	 * @return The file upload directory or null
	 */
	public abstract File getFileUploadDirectory();

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 * Defaults to zero, meaning that all uploaded files will be stored to disk.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>fileUploadSizeThreshold</code> in the application's {@link Context}.
	 * 
	 * @return The file upload size threshold
	 */
	public abstract int getFileUploadSizeThreshold();

	/**
	 * Cache used for caching mode. It is stored in the application's
	 * {@link Context} for persistence across requests and for sharing among
	 * instances of {@link GeneratedTextResource}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.cache</code> in the application's
	 * {@link Context}.
	 * <p>
	 * Note that this instance is shared with {@link DelegatedResource}.
	 * 
	 * @return The cache or null
	 */
	public abstract Cache getCache();

	//
	// Operations
	//

	public void addLibraryLocations( ExecutionContext executionContext )
	{
		// Add library locations
		DocumentSource<Executable> source = getLibrariesDocumentSource();
		if( source instanceof DocumentFileSource<?> )
		{
			File libraryDirectory = ( (DocumentFileSource<Executable>) source ).getBasePath();
			if( libraryDirectory != null )
				executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
		}

		source = getCommonLibrariesDocumentSource();
		if( source instanceof DocumentFileSource<?> )
		{
			File libraryDirectory = ( (DocumentFileSource<Executable>) source ).getBasePath();
			if( libraryDirectory != null )
				executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
		}
	}

	/**
	 * Throws an exception if the document name is invalid. Uses
	 * {@link #getDefaultName()} if no name is given, and respects
	 * {@link #isTrailingSlashRequired()}.
	 * 
	 * @param documentName
	 *        The document name
	 * @return The valid document name
	 * @throws ResourceException
	 */
	public String validateDocumentName( String documentName ) throws ResourceException
	{
		return validateDocumentName( documentName, getDefaultName() );
	}

	/**
	 * Throws an exception if the document name is invalid. Uses the default
	 * given document name if no name is given, and respects
	 * {@link #isTrailingSlashRequired()}.
	 * 
	 * @param documentName
	 *        The document name
	 * @param defaultDocumentName
	 *        The default document name
	 * @return The valid document name
	 * @throws ResourceException
	 */
	public String validateDocumentName( String documentName, String defaultDocumentName ) throws ResourceException
	{
		if( isTrailingSlashRequired() )
			if( ( documentName != null ) && ( documentName.length() != 0 ) && !documentName.endsWith( "/" ) )
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

		if( ( documentName == null ) || ( documentName.length() == 0 ) || ( documentName.equals( "/" ) ) )
		{
			documentName = defaultDocumentName;
			if( isTrailingSlashRequired() && !documentName.endsWith( "/" ) )
				documentName += "/";
		}

		return documentName;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The prefix for attribute keys.
	 */
	protected final String prefix;
}
