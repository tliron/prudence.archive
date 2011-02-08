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
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.internal.ContextualAttributes;
import com.threecrickets.prudence.internal.DelegatedHandlerAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.DelegatedHandlerConversationService;
import com.threecrickets.prudence.service.DelegatedHandlerDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
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
 * {@link String}, defaults to "javascript". See
 * {@link #getDefaultLanguageTag()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedHandler.defaultName:</code>
 * {@link String}, defaults to "default". See {@link #getDefaultIncludedName()}.
 * </li>
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
	 * @param context
	 *        The context used to configure the handler
	 */
	public DelegatedHandler( String documentName, Context context )
	{
		attributes = new DelegatedHandlerAttributes( context );
		this.documentName = documentName;
	}

	//
	// Attributes
	//

	/**
	 * The attributes.
	 * 
	 * @return The attributes
	 */
	public ContextualAttributes getAttributes()
	{
		return attributes;
	}

	/**
	 * @return The document name
	 */
	public String getDocumentName()
	{
		return documentName;
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
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( getDocumentName(), attributes.getDocumentSource(), false, attributes.getLanguageManager(), attributes.getDefaultLanguageTag(),
				attributes.isPrepare() );
			Executable executable = documentDescriptor.getDocument();

			if( executable.getEnterableExecutionContext() == null )
			{
				ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
				attributes.addLibraryLocations( executionContext );

				executionContext.getServices().put( attributes.getDocumentServiceName(), new DelegatedHandlerDocumentService( this ) );
				executionContext.getServices().put( attributes.getApplicationServiceName(), new ApplicationService() );

				try
				{
					if( !executable.makeEnterable( executionContext, this, attributes.getExecutionController() ) )
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
			entryPointValidityCache = attributes.getEntryPointValidityCache( executable );
			Boolean isValid = entryPointValidityCache.get( entryPointName );
			if( ( isValid != null ) && !isValid.booleanValue() )
				throw new NoSuchMethodException( entryPointName );

			// Enter!
			Object r;
			if( arguments != null )
			{
				ArrayList<Object> argumentList = new ArrayList<Object>( arguments.length + 1 );
				argumentList.add( new DelegatedHandlerConversationService( attributes.getFileUploadSizeThreshold(), attributes.getFileUploadDirectory() ) );
				for( Object argument : arguments )
					argumentList.add( argument );
				r = executable.enter( entryPointName, argumentList.toArray() );
			}
			else
				r = executable.enter( entryPointName, new DelegatedHandlerConversationService( attributes.getFileUploadSizeThreshold(), attributes.getFileUploadDirectory() ) );

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
				attributes.getWriter().flush();
				attributes.getErrorWriter().flush();
			}
			catch( IOException x )
			{
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The attributes.
	 */
	private final DelegatedHandlerAttributes attributes;

	/**
	 * The document name.
	 */
	private final String documentName;
}
