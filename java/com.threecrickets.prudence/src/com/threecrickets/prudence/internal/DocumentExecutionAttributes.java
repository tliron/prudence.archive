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

package com.threecrickets.prudence.internal;

import org.restlet.Context;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;

public interface DocumentExecutionAttributes
{
	//
	// Attributes
	//

	/**
	 * The {@link DocumentSource} used to fetch documents. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentSource</code> in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	public DocumentSource<Executable> getDocumentSource();

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>languageManager</code> in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager();

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "javascript".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultLanguageTag</code> in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag();

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
	public boolean isPrepare();

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
	public DocumentSource<Executable> getLibrariesDocumentSource();

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
	public DocumentSource<Executable> getCommonLibrariesDocumentSource();

	//
	// Operations
	//

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
	public String validateDocumentName( String documentName );
}
