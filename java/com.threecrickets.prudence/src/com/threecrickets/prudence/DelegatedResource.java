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
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.DelegatedResourceConversationService;
import com.threecrickets.prudence.service.DelegatedResourceDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * A Restlet resource which delegates functionality to a Scripturian
 * {@link Executable} via entry points. The entry points must be global
 * functions, closures, or whatever other technique the language engine uses to
 * for entry points. Supported entry points are:
 * <ul>
 * <li><code>handleInit()</code></li>
 * <li><code>handleGet()</code></li>
 * <li><code>handleGetInfo()</code></li>
 * <li><code>handlePost()</code></li>
 * <li><code>handlePut()</code></li>
 * <li><code>handleDelete()</code></li>
 * <li><code>handleOptions()</code></li>
 * </ul>
 * <p>
 * A <code>conversation</code> service is sent as an argument to all entry
 * points. Additionally, <code>document</code> and <code>application</code>
 * services are available as global services. See
 * {@link DelegatedResourceConversationService},
 * {@link DelegatedResourceDocumentService} and {@link ApplicationService}.
 * <p>
 * Before using this resource, make sure to configure a valid document source in
 * the application's {@link Context}; see {@link #getDocumentSource()}. This
 * document source is exposed to the executable as <code>document.source</code>.
 * <p>
 * Note that the executable's output is sent to the system's standard output.
 * Most likely, you will not want to output anything from the executable.
 * However, this redirection is provided as a debugging convenience.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.cache:</code> {@link Cache}. See
 * {@link #getCache()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.applicationServiceName</code>
 * : The name of the global variable with which to access the application
 * service. Defaults to "application". See {@link #getApplicationServiceName()}.
 * </li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultLanguageTag:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultLanguageTag()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.defaultName:</code>
 * {@link String}, defaults to "default". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentFormatter:</code>
 * {@link DocumentFormatter}. Defaults to a {@link JygmentsDocumentFormatter}.
 * See {@link #getDocumentFormatter()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentServiceName</code>
 * : The name of the global variable with which to access the document service.
 * Defaults to "document". See {@link #getDocumentServiceName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b> See {@link #getDocumentSource()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete:</code>
 * {@link String}, defaults to "handleDelete". See
 * {@link #getEntryPointNameForDelete()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGet:</code>
 * {@link String}, defaults to "handleGet". See
 * {@link #getEntryPointNameForGet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo:</code>
 * {@link String}, defaults to "handleGetInfo". See
 * {@link #getEntryPointNameForGetInfo()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit:</code>
 * {@link String}, defaults to "handleInit". See
 * {@link #getEntryPointNameForInit()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions:</code>
 * {@link String}, defaults to "handleOptions". See
 * {@link #getEntryPointNameForOptions()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPost:</code>
 * {@link String}, defaults to "handlePost". See
 * {@link #getEntryPointNameForPost()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPut:</code>
 * {@link String}, defaults to "handlePut". See
 * {@link #getEntryPointNameForPut()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.errorWriter:</code>
 * {@link Writer}, defaults to standard error. See {@link #getErrorWriter()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.executionController:</code>
 * {@link ExecutionController}. See {@link #getExecutionController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.libraryDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../libraries/". See {@link #getLibraryDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance. See
 * {@link #getLanguageManager()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.prepare:</code>
 * {@link Boolean}, defaults to true. See {@link #isPrepare()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true. See {@link #isTrailingSlashRequired()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.writer:</code>
 * {@link Writer}, defaults to standard output. See {@link #getWriter()}.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 */
public class DelegatedResource extends ServerResource
{
	//
	// Attributes
	//

	/**
	 * The {@link Writer} used by the {@link Executable}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.writer</code> in the
	 * application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public Writer getWriter()
	{
		if( writer == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			writer = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedResource.writer" );

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
	 * <code>com.threecrickets.prudence.DelegatedResource.errorWriter</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public Writer getErrorWriter()
	{
		if( errorWriter == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			errorWriter = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedResource.errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.err );
		}

		return errorWriter;
	}

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.documentServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentServiceName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.documentServiceName" );

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
	 * <code>com.threecrickets.prudence.DelegatedResource.applicationServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			applicationServiceName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	/**
	 * General-purpose cache. It is stored in the application's {@link Context}
	 * for persistence across requests and for sharing among instances of
	 * {@link DelegatedResource}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.cache</code> in the application's
	 * {@link Context}.
	 * <p>
	 * Note that this instance is shared with {@link GeneratedTextResource}.
	 * 
	 * @return The cache or null
	 * @see DelegatedResource#getCache()
	 */
	public Cache getCache()
	{
		if( cache == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			cache = (Cache) attributes.get( "com.threecrickets.prudence.cache" );
		}

		return cache;
	}

	/**
	 * The default character set to be used if the client does not specify it.
	 * Defaults to {@link CharacterSet#UTF_8}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public CharacterSet getDefaultCharacterSet()
	{
		if( defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "default".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultName</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultName" );

			if( defaultName == null )
				defaultName = "default";
		}

		return defaultName;
	}

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultLanguageTag</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag()
	{
		if( defaultLanguageTag == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultLanguageTag = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "js";
		}

		return defaultLanguageTag;
	}

	/**
	 * The name of the <code>handleInit()</code> entry point in the executable.
	 * Defaults to "handleInit".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleInit()</code> entry point
	 */
	public String getEntryPointNameForInit()
	{
		if( entryPointNameForInit == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForInit = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForInit" );

			if( entryPointNameForInit == null )
				entryPointNameForInit = "handleInit";
		}

		return entryPointNameForInit;
	}

	/**
	 * The name of the <code>handleGet()</code> entry point in the executable.
	 * Defaults to "handleGet".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleGet()</code> entry point
	 */
	public String getEntryPointNameForGet()
	{
		if( entryPointNameForGet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForGet = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForGet" );

			if( entryPointNameForGet == null )
				entryPointNameForGet = "handleGet";
		}

		return entryPointNameForGet;
	}

	/**
	 * The name of the <code>handleGetInfo()</code> entry point in the
	 * executable. Defaults to "handleGetInfo".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleGetInfo()</code> entry point
	 */
	public String getEntryPointNameForGetInfo()
	{
		if( entryPointNameForGetInfo == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForGetInfo = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo" );

			if( entryPointNameForGetInfo == null )
				entryPointNameForGetInfo = "handleGetInfo";
		}

		return entryPointNameForGetInfo;
	}

	/**
	 * The name of the <code>handleOptions()</code> entry point in the
	 * executable. Defaults to "handleOptions".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleOptions()</code> entry point
	 */
	public String getEntryPointNameForOptions()
	{
		if( entryPointNameForOptions == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForOptions = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions" );

			if( entryPointNameForOptions == null )
				entryPointNameForOptions = "handleOptions";
		}

		return entryPointNameForOptions;
	}

	/**
	 * The name of the <code>handlePost()</code> entry point in the executable.
	 * Defaults to "handlePost".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPost</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handlePost()</code> entry point
	 */
	public String getEntryPointNameForPost()
	{
		if( entryPointNameForPost == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForPost = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForPost" );

			if( entryPointNameForPost == null )
				entryPointNameForPost = "handlePost";
		}

		return entryPointNameForPost;
	}

	/**
	 * The name of the <code>handlePut()</code> entry point in the executable.
	 * Defaults to "handlePut".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPut</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handlePut()</code> entry point
	 */
	public String getEntryPointNameForPut()
	{
		if( entryPointNameForPut == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForPut = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForPut" );

			if( entryPointNameForPut == null )
				entryPointNameForPut = "handlePut";
		}

		return entryPointNameForPut;
	}

	/**
	 * The name of the <code>handleDelete()</code> entry point in the
	 * executable. Defaults to "handleDelete".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleDelete()</code> entry point
	 */
	public String getEntryPointNameForDelete()
	{
		if( entryPointNameForDelete == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForDelete = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete" );

			if( entryPointNameForDelete == null )
				entryPointNameForDelete = "handleDelete";
		}

		return entryPointNameForDelete;
	}

	/**
	 * An optional {@link ExecutionController} to be used with the executable.
	 * Useful for exposing your own global variables to the executable.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.executionController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public ExecutionController getExecutionController()
	{
		if( executionController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			executionController = (ExecutionController) attributes.get( "com.threecrickets.prudence.DelegatedResource.executionController" );
		}

		return executionController;
	}

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.languageManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			languageManager = (LanguageManager) attributes.get( "com.threecrickets.prudence.DelegatedResource.languageManager" );

			if( languageManager == null )
			{
				languageManager = new LanguageManager();

				LanguageManager existing = (LanguageManager) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.languageManager", languageManager );
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
	 * <code>com.threecrickets.prudence.DelegatedResource.documentSource</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getDocumentSource()
	{
		if( documentSource == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentSource = (DocumentSource<Executable>) attributes.get( "com.threecrickets.prudence.DelegatedResource.documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.DelegatedResource.documentSource must be set in context to use ScriptResource" );
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
	 * <code>com.threecrickets.prudence.DelegatedResource.libraryDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The library directory or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public File getLibraryDirectory()
	{
		if( libraryDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			libraryDirectory = (File) attributes.get( "com.threecrickets.prudence.DelegatedResource.libraryDirectory" );

			if( libraryDirectory == null )
			{
				if( documentSource instanceof DocumentFileSource<?> )
				{
					libraryDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../libraries/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.libraryDirectory", libraryDirectory );
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
	 * <code>com.threecrickets.prudence.DelegatedResource.trailingSlashRequired</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isTrailingSlashRequired()
	{
		if( trailingSlashRequired == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			trailingSlashRequired = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.trailingSlashRequired" );

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
	 * <code>com.threecrickets.prudence.DelegatedResource.prepare</code> in the
	 * application's {@link Context}.
	 * 
	 * @return Whether to prepare executables
	 */
	public boolean isPrepare()
	{
		if( prepare == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			prepare = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.prepare" );

			if( prepare == null )
				prepare = true;
		}

		return prepare;
	}

	/**
	 * This is so we can see the source code for documents by adding
	 * <code>?source=true</code> to the URL. You probably wouldn't want this for
	 * most applications. Defaults to false.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of source code
	 */
	public boolean isSourceViewable()
	{
		if( sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.sourceViewable" );

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
	 * <code>com.threecrickets.prudence.DelegatedResource.documentFormatter</code>
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
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentFormatter = (DocumentFormatter<Executable>) attributes.get( "com.threecrickets.prudence.DelegatedResource.documentFormatter" );

			if( documentFormatter == null )
			{
				documentFormatter = new JygmentsDocumentFormatter<Executable>();

				DocumentFormatter<Executable> existing = (DocumentFormatter<Executable>) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.documentFormatter", documentFormatter );
				if( existing != null )
					documentFormatter = existing;
			}
		}

		return documentFormatter;
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
	// ServerResource
	//

	/**
	 * Initializes the resource, and delegates to the <code>handleInit()</code>
	 * entry point in the executable.
	 * 
	 * @see #getEntryPointNameForInit()
	 */
	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();
		setAnnotated( false );

		if( isSourceViewable() )
		{
			Request request = getRequest();
			Form query = request.getResourceRef().getQueryAsForm();
			if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
				// Bypass doInit delegation
				return;
		}

		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, null, getDefaultCharacterSet() );
		enter( getEntryPointNameForInit(), conversationService );
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForGet()
	 */
	@Override
	public Representation get() throws ResourceException
	{
		return get( null );
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForGet()
	 */
	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		if( isSourceViewable() )
		{
			Request request = getRequest();
			Form query = request.getResourceRef().getQueryAsForm();
			if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
			{
				String documentName = request.getResourceRef().getRemainingPart( true, false );
				documentName = validateDocumentName( documentName );
				int lineNumber = -1;
				String line = query.getFirstValue( HIGHLIGHT );
				if( line != null )
				{
					try
					{
						lineNumber = Integer.parseInt( line );
					}
					catch( NumberFormatException x )
					{
					}
				}
				try
				{
					DocumentDescriptor<Executable> documentDescriptor = getDocumentSource().getDocument( documentName );
					DocumentFormatter<Executable> documentFormatter = getDocumentFormatter();
					if( documentFormatter != null )
						return new StringRepresentation( documentFormatter.format( documentDescriptor, documentName, lineNumber ), MediaType.TEXT_HTML );
					else
						return new StringRepresentation( documentDescriptor.getSourceCode() );
				}
				catch( DocumentNotFoundException x )
				{
					throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
				}
				catch( DocumentException x )
				{
					throw new ResourceException( x );
				}
			}
		}

		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, getDefaultCharacterSet() );
		Object r = enter( getEntryPointNameForGet(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result info
	 * @throws ResourceException
	 * @see #getEntryPointNameForGetInfo()
	 */
	@Override
	public RepresentationInfo getInfo() throws ResourceException
	{
		return getInfo( null );
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result info
	 * @throws ResourceException
	 * @see #getEntryPointNameForGetInfo()
	 */
	@Override
	public RepresentationInfo getInfo( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, getDefaultCharacterSet() );
		try
		{
			Object r = enter( getEntryPointNameForGetInfo(), conversationService );
			return getRepresentationInfo( r, conversationService );
		}
		catch( ResourceException x )
		{
			if( x.getCause() instanceof NoSuchMethodException )
				return get( variant );
			else
				throw x;
		}
	}

	/**
	 * Delegates to the <code>handlePost()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPost()
	 */
	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		return post( entity, null );
	}

	/**
	 * Delegates to the <code>handlePost()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPost()
	 */
	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, entity, variant, getDefaultCharacterSet() );
		Object r = enter( getEntryPointNameForPost(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPut()
	 */
	@Override
	public Representation put( Representation entity ) throws ResourceException
	{
		return put( entity, null );
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPut()
	 */
	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, entity, variant, getDefaultCharacterSet() );
		Object r = enter( getEntryPointNameForPut(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForDelete()
	 */
	@Override
	public Representation delete() throws ResourceException
	{
		return delete( null );
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the
	 * executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForDelete()
	 */
	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, getDefaultCharacterSet() );
		enter( getEntryPointNameForDelete(), conversationService );
		return null;
	}

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForOptions()
	 */
	@Override
	public Representation options() throws ResourceException
	{
		return options( null );
	}

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the
	 * executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForOptions()
	 */
	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, getDefaultCharacterSet() );
		Object r = enter( getEntryPointNameForOptions(), conversationService );
		return getRepresentation( r, conversationService );
	}

	@Override
	public void doRelease()
	{
		super.doRelease();
		ExecutionContext.disconnect();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	private volatile LanguageManager languageManager;

	/**
	 * Whether or not trailing slashes are required for all requests.
	 */
	private volatile Boolean trailingSlashRequired;

	/**
	 * Whether to prepare executables.
	 */
	private volatile Boolean prepare;

	/**
	 * The {@link DocumentSource} used to fetch executables.
	 */
	private volatile DocumentSource<Executable> documentSource;

	/**
	 * Executables might use directory this for importing libraries.
	 */
	private volatile File libraryDirectory;

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
	 * The default character set to be used if the client does not specify it.
	 */
	private volatile CharacterSet defaultCharacterSet;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	private volatile ExecutionController executionController;

	/**
	 * The name of the <code>handleInit()</code> entry point in the executable.
	 */
	private volatile String entryPointNameForInit;

	/**
	 * The name of the <code>handleGet()</code> entry point in the executable.
	 */
	private volatile String entryPointNameForGet;

	/**
	 * The name of the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForGetInfo;

	/**
	 * The name of the <code>handleOptions()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForOptions;

	/**
	 * The name of the <code>handlePost()</code> entry point in the executable.
	 */
	private volatile String entryPointNameForPost;

	/**
	 * The name of the <code>handlePut()</code> entry point in the executable.
	 */
	private volatile String entryPointNameForPut;

	/**
	 * The name of the <code>handleDelete()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForDelete;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	private volatile Boolean sourceViewable;

	/**
	 * General-purpose cache.
	 */
	private volatile Cache cache;

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	private volatile Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private volatile Writer errorWriter = new OutputStreamWriter( System.err );

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
	 * The document formatter.
	 */
	private volatile DocumentFormatter<Executable> documentFormatter;

	/**
	 * Constant.
	 */
	private static final String SOURCE = "source";

	/**
	 * Constant.
	 */
	private static final String HIGHLIGHT = "highlight";

	/**
	 * Constant.
	 */
	private static final String TRUE = "true";

	/**
	 * Returns a representation based on the object. If the object is not
	 * already a representation, creates a new string representation based on
	 * the container's attributes.
	 * 
	 * @param object
	 *        An object
	 * @param conversationService
	 *        The conversation service
	 * @return A representation
	 */
	private Representation getRepresentation( Object object, DelegatedResourceConversationService conversationService )
	{
		if( object == null )
			return null;
		else if( object instanceof Representation )
			return (Representation) object;
		else if( object instanceof Number )
		{
			// Returning a number means setting the status
			getResponse().setStatus( Status.valueOf( ( (Number) object ).intValue() ) );
			return null;
		}
		else
		{
			Representation representation = new StringRepresentation( object.toString(), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet() );
			representation.setTag( conversationService.getTag() );
			representation.setExpirationDate( conversationService.getExpirationDate() );
			representation.setModificationDate( conversationService.getModificationDate() );
			return representation;
		}
	}

	/**
	 * Returns a representation info based on the object. If the object is not
	 * already a representation info, creates a new representation info based on
	 * the container's attributes.
	 * 
	 * @param object
	 *        An object
	 * @param conversationService
	 *        The conversation service
	 * @return A representation info
	 */
	private RepresentationInfo getRepresentationInfo( Object object, DelegatedResourceConversationService conversationService )
	{
		if( object == null )
			return null;
		else if( object instanceof RepresentationInfo )
			return (RepresentationInfo) object;
		else if( object instanceof Date )
			return new RepresentationInfo( conversationService.getMediaType(), (Date) object );
		else if( object instanceof Number )
			return new RepresentationInfo( conversationService.getMediaType(), new Date( ( (Number) object ).longValue() ) );
		else if( object instanceof Tag )
			return new RepresentationInfo( conversationService.getMediaType(), (Tag) object );
		else if( object instanceof String )
			return new RepresentationInfo( conversationService.getMediaType(), Tag.parse( (String) object ) );
		else
			throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "cannot convert " + object.getClass().toString() + " to a RepresentationInfo" );
	}

	/**
	 * Enters the executable.
	 * 
	 * @param entryPointName
	 *        Name of entry point
	 * @param conversationService
	 *        The conversation service
	 * @return Result of invocation
	 * @throws ResourceException
	 * @see {@link Executable#invoke(String, Object...)}
	 */
	private Object enter( String entryPointName, DelegatedResourceConversationService conversationService ) throws ResourceException
	{
		String documentName = getRequest().getResourceRef().getRemainingPart( true, false );
		documentName = validateDocumentName( documentName );

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, getDocumentSource(), false, getLanguageManager(), getDefaultLanguageTag(), isPrepare() );
			Executable executable = documentDescriptor.getDocument();

			if( executable.getEnterableExecutionContext() == null )
			{
				ExecutionContext executionContext = new ExecutionContext( getWriter(), getErrorWriter() );

				File libraryDirectory = getLibraryDirectory();
				if( libraryDirectory != null )
					executionContext.getLibraryLocations().add( libraryDirectory.toURI() );

				executionContext.getServices().put( getDocumentServiceName(), new DelegatedResourceDocumentService( this ) );
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

			// Enter!
			Object r = executable.enter( entryPointName, conversationService );
			return r;
		}
		catch( DocumentNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
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
			throw new ResourceException( x );
		}
		finally
		{
			try
			{
				getWriter().flush();
				getErrorWriter().flush();
			}
			catch( IOException x )
			{
			}
		}
	}
}