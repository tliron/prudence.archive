package com.threecrickets.prudence.internal;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

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

public class VolatileContextualAttributes<R extends ServerResource> implements DocumentExecutionAttributes
{
	//
	// Construction
	//

	public VolatileContextualAttributes( R resource )
	{
		this.resource = resource;
		prefix = resource.getClass().getCanonicalName();
	}

	//
	// Attributes
	//

	/**
	 * The {@link Writer} used by the {@link Executable}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>writer</code> in the application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public Writer getWriter()
	{
		if( writer == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			writer = (Writer) attributes.get( prefix + ".writer" );

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
	 * <code>errorWriter</code> in the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public Writer getErrorWriter()
	{
		if( errorWriter == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			errorWriter = (Writer) attributes.get( prefix + ".errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.err );
		}

		return errorWriter;
	}

	/**
	 * The {@link DocumentSource} used to fetch documents. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentSource</code> in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getDocumentSource()
	{
		if( documentSource == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			documentSource = (DocumentSource<Executable>) attributes.get( prefix + ".documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute documentSource must be set in context to use GeneratedTextResource" );
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
	 * <code>libraryDirectory</code> in the application's {@link Context}.
	 * 
	 * @return The library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getLibrariesDocumentSource()
	{
		if( librariesDocumentSource == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			librariesDocumentSource = (DocumentSource<Executable>) attributes.get( prefix + ".librariesDocumentSource" );
		}

		return librariesDocumentSource;
	}

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
	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getCommonLibrariesDocumentSource()
	{
		if( commonLibrariesDocumentSource == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			commonLibrariesDocumentSource = (DocumentSource<Executable>) attributes.get( prefix + ".commonLibrariesDocumentSource" );
		}

		return commonLibrariesDocumentSource;
	}

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
	public String getDefaultName()
	{
		if( defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			defaultName = (String) attributes.get( prefix + ".defaultName" );

			if( defaultName == null )
				defaultName = "default";
		}

		return defaultName;
	}

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "javascript".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultLanguageTag</code> in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag()
	{
		if( defaultLanguageTag == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			defaultLanguageTag = (String) attributes.get( prefix + ".defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "javascript";
		}

		return defaultLanguageTag;
	}

	/**
	 * Whether or not trailing slashes are required. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>trailingSlashRequired</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isTrailingSlashRequired()
	{
		if( trailingSlashRequired == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			trailingSlashRequired = (Boolean) attributes.get( prefix + ".trailingSlashRequired" );

			if( trailingSlashRequired == null )
				trailingSlashRequired = true;
		}

		return trailingSlashRequired;
	}

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			documentServiceName = (String) attributes.get( prefix + ".documentServiceName" );

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
	 * <code>applicationServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			applicationServiceName = (String) attributes.get( prefix + ".applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>languageManager</code> in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			languageManager = (LanguageManager) attributes.get( prefix + ".languageManager" );

			if( languageManager == null )
			{
				languageManager = new LanguageManager();

				LanguageManager existing = (LanguageManager) attributes.putIfAbsent( prefix + ".languageManager", languageManager );
				if( existing != null )
					languageManager = existing;
			}
		}

		return languageManager;
	}

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
	public boolean isPrepare()
	{
		if( prepare == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			prepare = (Boolean) attributes.get( prefix + ".prepare" );

			if( prepare == null )
				prepare = true;
		}

		return prepare;
	}

	/**
	 * An optional {@link ExecutionController} to be used with the executable.
	 * Useful for exposing your own global variables to the executable.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>executionController</code> in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public ExecutionController getExecutionController()
	{
		if( executionController == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			executionController = (ExecutionController) attributes.get( prefix + ".executionController" );
		}

		return executionController;
	}

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
	public boolean isSourceViewable()
	{
		if( sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			sourceViewable = (Boolean) attributes.get( prefix + ".sourceViewable" );

			if( sourceViewable == null )
				sourceViewable = false;
		}

		return sourceViewable;
	}

	/**
	 * An optional {@link DocumentFormatter} to use for representing source
	 * code. Defaults to a {@link JygmentsDocumentFormatter}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedtextResource.documentFormatter</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document formatter or null
	 * @see #isSourceViewable()
	 */
	@SuppressWarnings("unchecked")
	public DocumentFormatter<Executable> getDocumentFormatter()
	{
		if( documentFormatter == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			documentFormatter = (DocumentFormatter<Executable>) attributes.get( prefix + ".documentFormatter" );

			if( documentFormatter == null )
			{
				documentFormatter = new JygmentsDocumentFormatter<Executable>();

				DocumentFormatter<Executable> existing = (DocumentFormatter<Executable>) attributes.putIfAbsent( prefix + ".documentFormatter", documentFormatter );
				if( existing != null )
					documentFormatter = existing;
			}
		}

		return documentFormatter;
	}

	/**
	 * The default character set to be used if the client does not specify it.
	 * Defaults to {@link CharacterSet#UTF_8}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultCharacterSet</code> in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public CharacterSet getDefaultCharacterSet()
	{
		if( defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			defaultCharacterSet = (CharacterSet) attributes.get( prefix + ".defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

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
	public File getFileUploadDirectory()
	{
		if( fileUploadDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			fileUploadDirectory = (File) attributes.get( prefix + ".fileUploadDirectory" );

			if( fileUploadDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					fileUploadDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../uploads/" );

					File existing = (File) attributes.putIfAbsent( prefix + ".fileUploadDirectory", fileUploadDirectory );
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
	 * <code>fileUploadSizeThreshold</code> in the application's {@link Context}.
	 * 
	 * @return The file upload size threshold
	 */
	public int getFileUploadSizeThreshold()
	{
		if( fileUploadSizeThreshold == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			Number number = (Number) attributes.get( prefix + ".fileUploadSizeThreshold" );
			if( number != null )
				fileUploadSizeThreshold = number.intValue();

			if( fileUploadSizeThreshold == null )
				fileUploadSizeThreshold = 0;
		}

		return fileUploadSizeThreshold;
	}

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
	 * @see DelegatedResource#getCache()
	 */
	public Cache getCache()
	{
		if( cache == null )
		{
			ConcurrentMap<String, Object> attributes = resource.getContext().getAttributes();
			cache = (Cache) attributes.get( "com.threecrickets.prudence.cache" );
		}

		return cache;
	}

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
	 * The resource.
	 */
	protected final R resource;

	/**
	 * The prefix for attribute keys.
	 */
	protected final String prefix;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	private volatile Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private volatile Writer errorWriter = new OutputStreamWriter( System.err );

	/**
	 * The document source.
	 */
	private volatile DocumentSource<Executable> documentSource;

	/**
	 * Executables might use directory this {@link DocumentSource} for importing
	 * libraries.
	 */
	private volatile DocumentSource<Executable> librariesDocumentSource;

	/**
	 * Executables from all applications might use this {@link DocumentSource}
	 * for importing libraries.
	 */
	private volatile DocumentSource<Executable> commonLibrariesDocumentSource;

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private volatile String defaultName;

	/**
	 * The default language tag to be used if the executable doesn't specify
	 * one.
	 */
	private volatile String defaultLanguageTag;

	/**
	 * Whether or not trailing slashes are required for all requests.
	 */
	private volatile Boolean trailingSlashRequired;

	/**
	 * The name of the global variable with which to access the document
	 * service.
	 */
	private volatile String documentServiceName;

	/**
	 * The name of the global variable with which to access the application
	 * service.
	 */
	private volatile String applicationServiceName;

	/**
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	private volatile LanguageManager languageManager;

	/**
	 * Whether to prepare executables.
	 */
	private volatile Boolean prepare;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	private volatile ExecutionController executionController;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	private volatile Boolean sourceViewable;

	/**
	 * The document formatter.
	 */
	private volatile DocumentFormatter<Executable> documentFormatter;

	/**
	 * The default character set to be used if the client does not specify it.
	 */
	private volatile CharacterSet defaultCharacterSet;

	/**
	 * The directory in which to place uploaded files.
	 */
	private volatile File fileUploadDirectory;

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	private volatile Integer fileUploadSizeThreshold;

	/**
	 * Cache used for caching mode.
	 */
	private volatile Cache cache;
}
