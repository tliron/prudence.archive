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

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.internal.attributes.DelegatedHandlerAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ConversationService;
import com.threecrickets.prudence.service.ConversationStoppedException;
import com.threecrickets.prudence.service.DocumentService;
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
 * {@link ConversationService}, {@link DocumentService} and
 * {@link ApplicationService}.
 * <p>
 * Before using this class, make sure to configure a valid document source in
 * the application's {@link Context} as
 * <code>com.threecrickets.prudence.DelegatedHandler.documentSource</code>. This
 * document source is exposed to the executable as <code>document.source</code>.
 * <p>
 * For a more sophisticated resource delegate, see {@link DelegatedResource}.
 * <p>
 * Instances are thread-safe.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.applicationServiceName</code>
 * : Defaults to "application".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.commonLibraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript".</li>
 * <li><code>com.threecrickets.prudence.DelegatedHandler.defaultName:</code>
 * {@link String}, defaults to "default".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.documentServiceName</code>:
 * Defaults to "document"..</li>
 * <li><code>com.threecrickets.prudence.DelegatedHandler.errorWriter:</code>
 * {@link Writer}, defaults to standard error.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.entryPointValidityCache:</code>
 * {@link ConcurrentMap}, default to a new {@link ConcurrentHashMap}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../uploads/".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold:</code>
 * {@link Integer}, defaults to zero.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.executionController:</code>
 * {@link ExecutionController}</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.libraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.prepare:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedHandler.writer:</code>
 * {@link Writer}, defaults to standard output.</li>
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
	 * Constructor.
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
	 * The attributes as configured in the {@link Application} context.
	 * 
	 * @return The attributes
	 */
	public DelegatedHandlerAttributes getAttributes()
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
			DocumentDescriptor<Executable> documentDescriptor = attributes.createDocumentOnce( documentName, false, true, true, false );
			Executable executable = documentDescriptor.getDocument();
			Object enteringKey = Application.getCurrent().hashCode();

			if( executable.getEnterableExecutionContext( enteringKey ) == null )
			{
				ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
				attributes.addLibraryLocations( executionContext );

				executionContext.getServices().put( attributes.getDocumentServiceName(), new DocumentService<DelegatedHandlerAttributes>( attributes, documentDescriptor ) );
				executionContext.getServices().put( attributes.getApplicationServiceName(), new ApplicationService() );

				try
				{
					if( !executable.makeEnterable( enteringKey, executionContext, this, attributes.getExecutionController() ) )
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
				argumentList.add( new ConversationService( attributes.getFileUploadSizeThreshold(), attributes.getFileUploadDirectory() ) );
				for( Object argument : arguments )
					argumentList.add( argument );
				r = executable.enter( enteringKey, entryPointName, argumentList.toArray() );
			}
			else
				r = executable.enter( enteringKey, entryPointName, new ConversationService( attributes.getFileUploadSizeThreshold(), attributes.getFileUploadDirectory() ) );

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
			Request request = Request.getCurrent();
			if( request != null )
			{
				if( ConversationStoppedException.isConversationStopped( request ) )
				{
					Application application = Application.getCurrent();
					if( application != null )
						application.getLogger().fine( "conversation.stop() was called" );

					return null;
				}
			}

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
	 * The attributes as configured in the {@link Application} context.
	 */
	private final DelegatedHandlerAttributes attributes;

	/**
	 * The document name.
	 */
	private final String documentName;
}
