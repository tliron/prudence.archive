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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.internal.GeneratedTextDeferredRepresentation;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.GeneratedTextResourceConversationService;
import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;
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
 * A Restlet resource which executes a "text with scriptlets" Scripturian
 * {@link Executable} document for GET and POST verbs and redirects its standard
 * output to a {@link StringRepresentation}.
 * <p>
 * <code>document</code>, <code>application</code> and <code>conversation</code>
 * are available as global services in scriptlets. See
 * {@link GeneratedTextResourceDocumentService}, {@link ApplicationService} and
 * {@link GeneratedTextResourceConversationService}.
 * <p>
 * Before using this resource, make sure to configure a valid document source in
 * the application's {@link Context}; see {@link #getDocumentSource()}. This
 * document source is exposed to scriptlets as <code>document.source</code>.
 * <p>
 * This resource supports caching into implementations of {@link Cache}. First,
 * the entire document is executed, with its output sent into a buffer. This
 * buffer is then cached, and <i>only then</i> sent to the client. Scriptlets
 * can control the duration of their individual cache by changing the value of
 * <code>document.cacheDuration</code>. Because output is not sent to the client
 * until after the executable finished its execution, it is possible for
 * scriptlets to set output characteristics at any time by changing the values
 * of <code>conversation.mediaType</code>,
 * <code>conversation.characterSet</code>, and
 * <code>conversation.language</code>.
 * <p>
 * There is experimental support for deferred response (asynchronous mode) via
 * <code>conversation.defer</code>.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.cache:</code> {@link Cache}. See
 * {@link #getCache()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.applicationServiceName</code>
 * : The name of the global variable with which to access the application
 * service. Defaults to "application". See {@link #getApplicationServiceName()}.
 * </li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.clientCachingMode:</code>
 * {@link Integer}, defaults to {@link #CLIENT_CACHING_MODE_CONDITIONAL}. See
 * {@link #getClientCachingMode()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.conversationServiceName</code>
 * : The name of the global variable with which to access the conversation
 * service. Defaults to "conversation". See
 * {@link #getConversationServiceName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCacheKey:</code>
 * {@link String}, defaults to "{ri}|{dn}". See {@link #getDefaultCacheKey()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultLanguageTag()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName:</code>
 * {@link String}, defaults to "index". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentFormatter:</code>
 * {@link DocumentFormatter}. Defaults to a {@link JygmentsDocumentFormatter}.
 * See {@link #getDocumentFormatter()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentServiceName</code>
 * : The name of the global variable with which to access the document service.
 * Defaults to "document". See {@link #getDocumentServiceName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b> See {@link #getDocumentSource()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.executionController:</code>
 * {@link ExecutionController}. See {@link #getExecutionController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.fragmentDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../fragments/". See {@link #getFragmentDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance. See
 * {@link #getLanguageManager()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.libraryDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../../libraries/". See {@link #getLibraryDirectory()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.prepare:</code>
 * {@link Boolean}, defaults to true. See {@link #isPrepare()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true. See {@link #isTrailingSlashRequired()}.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 */
public class GeneratedTextResource extends ServerResource
{
	//
	// Constants
	//

	public static final int CLIENT_CACHING_MODE_DISABLED = 0;

	public static final int CLIENT_CACHING_MODE_CONDITIONAL = 1;

	public static final int CLIENT_CACHING_MODE_OFFLINE = 2;

	//
	// Attributes
	//

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.documentServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentServiceName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.documentServiceName" );

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
	 * <code>com.threecrickets.prudence.GeneratedTextResource.applicationServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			applicationServiceName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	/**
	 * The name of the global variable with which to access the conversation
	 * service. Defaults to "conversation".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.conversationServiceName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The conversation service name
	 */
	public String getConversationServiceName()
	{
		if( conversationServiceName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			conversationServiceName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.conversationServiceName" );

			if( conversationServiceName == null )
				conversationServiceName = "conversation";
		}

		return conversationServiceName;
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
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public CharacterSet getDefaultCharacterSet()
	{
		if( defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

	/**
	 * If a URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs that do not contain
	 * filenames. Defaults to "index".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultName" );

			if( defaultName == null )
				defaultName = "index";
		}

		return defaultName;
	}

	/**
	 * The default language tag to use if the first scriptlet doesn't specify
	 * one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The language tag
	 */
	public String getDefaultLanguageTag()
	{
		if( defaultLanguageTag == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultLanguageTag = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "js";
		}

		return defaultLanguageTag;
	}

	/**
	 * The default cache key to use if the executable doesn't specify one.
	 * Defaults to "{ri}|{dn}".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCacheKey</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default cache key
	 */
	public String getDefaultCacheKey()
	{
		if( defaultCacheKey == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultCacheKey = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultCacheKey" );

			if( defaultCacheKey == null )
				defaultCacheKey = "{ri}|{dn}";
		}

		return defaultCacheKey;
	}

	/**
	 * An optional {@link ExecutionController} to be used with the scriptlets.
	 * Useful for exposing your own global variables to the scriptlets.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.executionController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public ExecutionController getExecutionController()
	{
		if( executionController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			executionController = (ExecutionController) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.executionController" );
		}

		return executionController;
	}

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.languageManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			languageManager = (LanguageManager) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.languageManager" );

			if( languageManager == null )
			{
				languageManager = new LanguageManager();

				LanguageManager existing = (LanguageManager) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.languageManager", languageManager );
				if( existing != null )
					languageManager = existing;
			}
		}

		return languageManager;
	}

	/**
	 * The {@link DocumentSource} used to fetch documents. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.documentSource</code>
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
			documentSource = (DocumentSource<Executable>) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.GeneratedTextResource.documentSource must be set in context to use GeneratedTextResource" );
		}

		return documentSource;
	}

	/**
	 * Executables might use this directory for including fragments. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../fragments/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.fragmentDirectory</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The fragment directory or null
	 */
	public File getFragmentDirectory()
	{
		if( fragmentDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			fragmentDirectory = (File) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.fragmentDirectory" );

			if( fragmentDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					fragmentDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../fragments/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.fragmentDirectory", fragmentDirectory );
					if( existing != null )
						fragmentDirectory = existing;
				}
			}
		}

		return fragmentDirectory;
	}

	/**
	 * If the {@link #getDocumentSource()} is a {@link DocumentFileSource}, then
	 * this is the fragment directory relative to the
	 * {@link DocumentFileSource#getBasePath()}. Otherwise, it's null.
	 * 
	 * @return The relative fragment directory or null
	 */
	public File getFragmentDirectoryRelative()
	{
		DocumentSource<Executable> documentSource = getDocumentSource();
		if( documentSource instanceof DocumentFileSource<?> )
		{
			File fragmentDirectory = getFragmentDirectory();
			if( fragmentDirectory != null )
				return ScripturianUtil.getRelativeFile( fragmentDirectory, ( (DocumentFileSource<?>) documentSource ).getBasePath() );
		}
		return null;
	}

	/**
	 * Executables might use this directory for importing libraries. If the
	 * {@link #getDocumentSource()} is a {@link DocumentFileSource}, then this
	 * will default to the {@link DocumentFileSource#getBasePath()} plus
	 * "../../libraries/".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.libraryDirectory</code>
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
			libraryDirectory = (File) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.libraryDirectory" );

			if( libraryDirectory == null )
			{
				DocumentSource<Executable> documentSource = getDocumentSource();
				if( documentSource instanceof DocumentFileSource<?> )
				{
					libraryDirectory = new File( ( (DocumentFileSource<?>) documentSource ).getBasePath(), "../../libraries/" );

					File existing = (File) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.libraryDirectory", libraryDirectory );
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
	 * <code>com.threecrickets.prudence.GeneratedTextResource.trailingSlashRequired</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isTrailingSlashRequired()
	{
		if( trailingSlashRequired == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			trailingSlashRequired = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.trailingSlashRequired" );

			if( trailingSlashRequired == null )
				trailingSlashRequired = true;
		}

		return trailingSlashRequired;
	}

	/**
	 * Whether or not to send information to the client about cache expiration.
	 * Defaults to {@link #CLIENT_CACHING_MODE_CONDITIONAL}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.clientCachingMode</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The client caching mode
	 */
	public int getClientCachingMode()
	{
		if( clientCachingMode == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			Number number = (Number) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.clientCachingMode" );

			if( number != null )
				clientCachingMode = number.intValue();

			if( clientCachingMode == null )
				clientCachingMode = CLIENT_CACHING_MODE_CONDITIONAL;
		}

		return clientCachingMode;
	}

	/**
	 * Whether to prepare the executables. Preparation increases initialization
	 * time and reduces execution time. Note that not all languages support
	 * preparation as a separate operation. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.prepare</code> in
	 * the application's {@link Context}.
	 * 
	 * @return Whether to prepare executables
	 */
	public boolean isPrepare()
	{
		if( prepare == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			prepare = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.prepare" );

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
	 * <code>com.threecrickets.prudence.GeneratedTextResource.sourceViewable</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of source code
	 */
	public boolean isSourceViewable()
	{
		if( sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.sourceViewable" );

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
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentFormatter = (DocumentFormatter<Executable>) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.documentFormatter" );

			if( documentFormatter == null )
			{
				documentFormatter = new JygmentsDocumentFormatter<Executable>();

				DocumentFormatter<Executable> existing = (DocumentFormatter<Executable>) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.documentFormatter", documentFormatter );
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
	 * Initializes the resource.
	 */
	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();
		setAnnotated( false );
	}

	@Override
	public Representation get() throws ResourceException
	{
		return execute( null, null );
	}

	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		return execute( null, variant );
	}

	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		return execute( entity, null );
	}

	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		return execute( entity, variant );
	}

	@Override
	public Representation put( Representation entity ) throws ResourceException
	{
		return execute( entity, null );
	}

	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		return execute( entity, variant );
	}

	@Override
	public Representation delete() throws ResourceException
	{
		return execute( null, null );
	}

	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		return execute( null, variant );
	}

	@Override
	public Representation options() throws ResourceException
	{
		return execute( null, null );
	}

	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		return execute( null, variant );
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
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	private volatile LanguageManager languageManager;

	/**
	 * The {@link DocumentSource} used to fetch scripts.
	 */
	private volatile DocumentSource<Executable> documentSource;

	/**
	 * Executables might use this directory for including fragments.
	 */
	private volatile File fragmentDirectory;

	/**
	 * Executables might use this directory for importing libraries.
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
	 * The default cache key to use if the executable doesn't specify one.
	 */
	private volatile String defaultCacheKey;

	/**
	 * The default character set to be used if the client does not specify it.
	 */
	private volatile CharacterSet defaultCharacterSet;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	private volatile ExecutionController executionController;

	/**
	 * Whether or not trailing slashes are required for all requests.
	 */
	private volatile Boolean trailingSlashRequired;

	/**
	 * Whether or not to send information to the client about cache expiration.
	 */
	private volatile Integer clientCachingMode;

	/**
	 * Whether to prepare executables.
	 */
	private volatile Boolean prepare;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	private volatile Boolean sourceViewable;

	/**
	 * Cache used for caching mode.
	 */
	private volatile Cache cache;

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
	 * The name of the global variable with which to access the conversation
	 * service.
	 */
	private volatile String conversationServiceName;

	/**
	 * The document formatter.
	 */
	private volatile DocumentFormatter<Executable> documentFormatter;

	/**
	 * Flag for asynchronous support (experimental).
	 */
	private final boolean asynchronousSupport = false;

	/**
	 * Does the actual handling of requests.
	 * 
	 * @param entity
	 *        The entity
	 * @param variant
	 *        The variant
	 * @return A representation
	 * @throws ResourceException
	 */
	private Representation execute( Representation entity, Variant variant ) throws ResourceException
	{
		Request request = getRequest();
		String documentName = request.getResourceRef().getRemainingPart( true, false );
		documentName = validateDocumentName( documentName );

		try
		{
			if( isSourceViewable() )
			{
				Form query = request.getResourceRef().getQueryAsForm();
				if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
				{
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

					DocumentDescriptor<Executable> documentDescriptor = getDocumentSource().getDocument( documentName );
					DocumentFormatter<Executable> documentFormatter = getDocumentFormatter();
					if( documentFormatter != null )
						return new StringRepresentation( documentFormatter.format( documentDescriptor, documentName, lineNumber ), MediaType.TEXT_HTML );
					else
						return new StringRepresentation( documentDescriptor.getSourceCode() );
				}
			}

			ExecutionContext executionContext = new ExecutionContext();
			File libraryDirectory = getLibraryDirectory();
			if( libraryDirectory != null )
				executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
			GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this, executionContext, entity, variant );
			Representation representation = null;
			try
			{
				// Execute and represent output
				representation = documentService.include( documentName, false );

				switch( getClientCachingMode() )
				{
					case CLIENT_CACHING_MODE_DISABLED:
					{
						// Remove all caching headers
						representation.setModificationDate( null );
						representation.setExpirationDate( null );
						representation.setTag( null );
						List<CacheDirective> cacheDirectives = getResponse().getCacheDirectives();
						cacheDirectives.clear();
						cacheDirectives.add( CacheDirective.noCache() );
						break;
					}

					case CLIENT_CACHING_MODE_CONDITIONAL:
						// Leave conditional headers intact
						break;

					case CLIENT_CACHING_MODE_OFFLINE:
					{
						// Add offline caching headers based on conditional
						// headers
						Date expirationDate = representation.getExpirationDate();
						if( expirationDate != null )
						{
							long maxAge = ( expirationDate.getTime() - System.currentTimeMillis() );
							if( maxAge > 0 )
							{
								List<CacheDirective> cacheDirectives = getResponse().getCacheDirectives();
								cacheDirectives.clear();
								cacheDirectives.add( CacheDirective.maxAge( (int) maxAge / 1000 ) );
							}
						}
						break;
					}
				}

				if( asynchronousSupport )
				{
					// Experimental

					if( representation instanceof GeneratedTextDeferredRepresentation )
					{
						setAutoCommitting( false );
						getApplication().getTaskService().submit( (GeneratedTextDeferredRepresentation) representation );
						return null;
					}
				}

				return representation;
			}
			catch( ParsingException x )
			{
				throw new ResourceException( x );
			}
			catch( ExecutionException x )
			{
				if( getResponse().getStatus().isSuccess() )
					// An unintended exception
					throw new ResourceException( x );
				else
					// This was an intended exception, so we will preserve the
					// status code
					return null;
			}
			finally
			{
				executionContext.release();
			}
		}
		catch( DocumentNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
		}
		catch( DocumentException x )
		{
			throw new ResourceException( x );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}
	}
}