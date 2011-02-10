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

package com.threecrickets.prudence.internal.attributes;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.CharacterSet;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;

public abstract class VolatileContextualAttributes extends ContextualAttributes
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param prefix
	 *        The prefix for attribute keys
	 */
	public VolatileContextualAttributes( String prefix )
	{
		super( prefix );
	}

	//
	// ContextualAttributes
	//

	@Override
	public Writer getWriter()
	{
		if( writer == null )
		{
			writer = (Writer) getAttributes().get( prefix + ".writer" );

			if( writer == null )
				writer = new OutputStreamWriter( System.out );
		}

		return writer;
	}

	@Override
	public Writer getErrorWriter()
	{
		if( errorWriter == null )
		{
			errorWriter = (Writer) getAttributes().get( prefix + ".errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.err );
		}

		return errorWriter;
	}

	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getDocumentSource()
	{
		if( documentSource == null )
		{
			documentSource = (DocumentSource<Executable>) getAttributes().get( prefix + ".documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute " + prefix + ".documentSource must be set in context" );
		}

		return documentSource;
	}

	@SuppressWarnings("unchecked")
	public Iterable<DocumentSource<Executable>> getLibraryDocumentSources()
	{
		if( libraryDocumentSources == null )
			libraryDocumentSources = (Iterable<DocumentSource<Executable>>) getAttributes().get( prefix + ".libraryDocumentSources" );

		return libraryDocumentSources;
	}

	public String getDefaultName()
	{
		if( defaultName == null )
		{
			defaultName = (String) getAttributes().get( prefix + ".defaultName" );

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
			defaultLanguageTag = (String) getAttributes().get( prefix + ".defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "javascript";
		}

		return defaultLanguageTag;
	}

	public boolean isTrailingSlashRequired()
	{
		if( trailingSlashRequired == null )
		{
			trailingSlashRequired = (Boolean) getAttributes().get( prefix + ".trailingSlashRequired" );

			if( trailingSlashRequired == null )
				trailingSlashRequired = true;
		}

		return trailingSlashRequired;
	}

	@Override
	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			documentServiceName = (String) getAttributes().get( prefix + ".documentServiceName" );

			if( documentServiceName == null )
				documentServiceName = "document";
		}

		return documentServiceName;
	}

	@Override
	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			applicationServiceName = (String) getAttributes().get( prefix + ".applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
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

	public boolean isPrepare()
	{
		if( prepare == null )
		{
			prepare = (Boolean) getAttributes().get( prefix + ".prepare" );

			if( prepare == null )
				prepare = true;
		}

		return prepare;
	}

	@Override
	public ExecutionController getExecutionController()
	{
		if( executionController == null )
			executionController = (ExecutionController) getAttributes().get( prefix + ".executionController" );

		return executionController;
	}

	@Override
	public boolean isSourceViewable()
	{
		if( sourceViewable == null )
		{
			sourceViewable = (Boolean) getAttributes().get( prefix + ".sourceViewable" );

			if( sourceViewable == null )
				sourceViewable = false;
		}

		return sourceViewable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentFormatter<Executable> getDocumentFormatter()
	{
		if( documentFormatter == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
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

	@Override
	public CharacterSet getDefaultCharacterSet()
	{
		if( defaultCharacterSet == null )
		{
			defaultCharacterSet = (CharacterSet) getAttributes().get( prefix + ".defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

	@Override
	public File getFileUploadDirectory()
	{
		if( fileUploadDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
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

	@Override
	public int getFileUploadSizeThreshold()
	{
		if( fileUploadSizeThreshold == null )
		{
			Number number = (Number) getAttributes().get( prefix + ".fileUploadSizeThreshold" );

			if( number != null )
				fileUploadSizeThreshold = number.intValue();

			if( fileUploadSizeThreshold == null )
				fileUploadSizeThreshold = 0;
		}

		return fileUploadSizeThreshold;
	}

	@Override
	public Cache getCache()
	{
		if( cache == null )
			cache = (Cache) getAttributes().get( "com.threecrickets.prudence.cache" );

		return cache;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	protected volatile Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	protected volatile Writer errorWriter = new OutputStreamWriter( System.err );

	/**
	 * The document source.
	 */
	protected volatile DocumentSource<Executable> documentSource;

	/**
	 * Executables might use these {@link DocumentSource} instances for
	 * importing libraries.
	 */
	protected volatile Iterable<DocumentSource<Executable>> libraryDocumentSources;

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	protected volatile String defaultName;

	/**
	 * The default language tag to be used if the executable doesn't specify
	 * one.
	 */
	protected volatile String defaultLanguageTag;

	/**
	 * Whether or not trailing slashes are required for all requests.
	 */
	protected volatile Boolean trailingSlashRequired;

	/**
	 * The name of the global variable with which to access the document
	 * service.
	 */
	protected volatile String documentServiceName;

	/**
	 * The name of the global variable with which to access the application
	 * service.
	 */
	protected volatile String applicationServiceName;

	/**
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	protected volatile LanguageManager languageManager;

	/**
	 * Whether to prepare executables.
	 */
	protected volatile Boolean prepare;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	protected volatile ExecutionController executionController;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	protected volatile Boolean sourceViewable;

	/**
	 * The document formatter.
	 */
	protected volatile DocumentFormatter<Executable> documentFormatter;

	/**
	 * The default character set to be used if the client does not specify it.
	 */
	protected volatile CharacterSet defaultCharacterSet;

	/**
	 * The directory in which to place uploaded files.
	 */
	protected static volatile File fileUploadDirectory;

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	protected static volatile Integer fileUploadSizeThreshold;

	/**
	 * Cache used for caching mode.
	 */
	protected static volatile Cache cache;
}
