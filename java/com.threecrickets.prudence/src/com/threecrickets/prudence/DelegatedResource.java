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
import java.io.Serializable;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.internal.attributes.DelegatedResourceAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ConversationStoppedException;
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

/**
 * A Restlet resource that delegates functionality to a Scripturian
 * {@link Executable} via entry points. The entry points must be global
 * functions, closures, or whatever other technique the language engine uses to
 * for entry points.
 * <p>
 * Supported entry points are:
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
 * services are available as global variables. See
 * {@link DelegatedResourceConversationService},
 * {@link DelegatedResourceDocumentService} and {@link ApplicationService}.
 * <p>
 * Before using this resource, make sure to configure a valid document source in
 * the application's {@link Context} as
 * <code>com.threecrickets.prudence.DelegatedResource.documentSource</code. This
 * document source is exposed to the executable as <code>document.source</code>.
 * <p>
 * For a simpler delegate, see {@link DelegatedHandler}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.cache:</code> {@link Cache}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.applicationServiceName</code>
 * : Defaults to "application".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.commonLibraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript".</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.defaultName:</code>
 * {@link String}, defaults to "default".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentFormatter:</code>
 * {@link DocumentFormatter}. Defaults to a {@link JygmentsDocumentFormatter}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentServiceName</code>
 * : Defaults to "document".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b></li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete:</code>
 * {@link String}, defaults to "handleDelete".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGet:</code>
 * {@link String}, defaults to "handleGet".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo:</code>
 * {@link String}, defaults to "handleGetInfo".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit:</code>
 * {@link String}, defaults to "handleInit".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions:</code>
 * {@link String}, defaults to "handleOptions".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPost:</code>
 * {@link String}, defaults to "handlePost".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPut:</code>
 * {@link String}, defaults to "handlePut".</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.errorWriter:</code>
 * {@link Writer}, defaults to standard error.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.executionController:</code>
 * {@link ExecutionController}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../uploads/".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold:</code>
 * {@link Integer}, defaults to zero.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.libraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.prepare:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.writer:</code>
 * {@link Writer}, defaults to standard output.</li>
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

	public DelegatedResourceAttributes getAttributes()
	{
		return attributes;
	}

	//
	// ServerResource
	//

	/**
	 * Initializes the resource, and delegates to the <code>handleInit()</code>
	 * entry point in the executable.
	 */
	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();
		setAnnotated( false );

		if( attributes.isSourceViewable() )
		{
			Request request = getRequest();
			Form query = request.getResourceRef().getQueryAsForm();
			if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
				// Bypass doInit delegation
				return;
		}

		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, null, attributes.getDefaultCharacterSet() );
		enter( attributes.getEntryPointNameForInit(), conversationService );
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
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
	 */
	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		if( attributes.isSourceViewable() )
		{
			Request request = getRequest();
			Form query = request.getResourceRef().getQueryAsForm();
			if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
			{
				String documentName = request.getResourceRef().getRemainingPart( true, false );
				documentName = attributes.validateDocumentName( documentName );
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
					DocumentDescriptor<Executable> documentDescriptor = attributes.getDocumentSource().getDocument( documentName );
					DocumentFormatter<Executable> documentFormatter = attributes.getDocumentFormatter();
					if( documentFormatter != null )
						return new StringRepresentation( documentFormatter.format( documentDescriptor, documentName, lineNumber ), MediaType.TEXT_HTML );
					else
						return new StringRepresentation( documentDescriptor.getSourceCode() );
				}
				catch( DocumentNotFoundException x )
				{
					throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
				}
				catch( DocumentException x )
				{
					throw new ResourceException( x );
				}
			}
		}

		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		Object r = enter( attributes.getEntryPointNameForGet(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result info
	 * @throws ResourceException
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
	 */
	@Override
	public RepresentationInfo getInfo( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		try
		{
			Object r = enter( attributes.getEntryPointNameForGetInfo(), conversationService );
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
	 */
	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, entity, variant, attributes.getDefaultCharacterSet() );
		Object r = enter( attributes.getEntryPointNameForPost(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
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
	 */
	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, entity, variant, attributes.getDefaultCharacterSet() );
		Object r = enter( attributes.getEntryPointNameForPut(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
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
	 */
	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		Object r = enter( attributes.getEntryPointNameForDelete(), conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
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
	 */
	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		Object r = enter( attributes.getEntryPointNameForOptions(), conversationService );
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
	 * The attributes.
	 */
	private final DelegatedResourceAttributes attributes = new DelegatedResourceAttributes( this );

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
		{
			return null;
		}
		else if( object instanceof Representation )
		{
			return (Representation) object;
		}
		else if( object instanceof Number )
		{
			// Returning a number means setting the status
			getResponse().setStatus( Status.valueOf( ( (Number) object ).intValue() ) );

			return null;
		}
		else
		{
			Representation representation;

			if( MediaType.APPLICATION_JAVA.equals( conversationService.getMediaType() ) )
			{
				// Wrap in an object representation
				representation = new ObjectRepresentation<Serializable>( (Serializable) object, MediaType.APPLICATION_JAVA );
				Language language = conversationService.getLanguage();
				if( language != null )
					representation.getLanguages().add( language );
				representation.setCharacterSet( conversationService.getCharacterSet() );
			}
			else
			{
				// Convert to string
				representation = new StringRepresentation( object.toString(), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet() );
			}

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
		RepresentationInfo representationInfo;
		if( object == null )
			return null;
		else if( object instanceof RepresentationInfo )
			return (RepresentationInfo) object;
		else if( object instanceof Date )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), (Date) object );
			representationInfo.setTag( conversationService.getTag() );
			return representationInfo;
		}
		else if( object instanceof Number )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), new Date( ( (Number) object ).longValue() ) );
			representationInfo.setTag( conversationService.getTag() );
			return representationInfo;
		}
		else if( object instanceof Tag )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), (Tag) object );
			representationInfo.setModificationDate( conversationService.getModificationDate() );
			return representationInfo;
		}
		else if( object instanceof String )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), Tag.parse( (String) object ) );
			representationInfo.setModificationDate( conversationService.getModificationDate() );
			return representationInfo;
		}
		else
			throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "cannot convert " + object.getClass().toString() + " to a RepresentationInfo" );
	}

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
		ConcurrentMap<String, Boolean> entryPointValidityCache = (ConcurrentMap<String, Boolean>) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointValidityCache" );
		if( entryPointValidityCache == null )
		{
			entryPointValidityCache = new ConcurrentHashMap<String, Boolean>();
			ConcurrentMap<String, Boolean> existing = (ConcurrentMap<String, Boolean>) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.entryPointValidityCache", entryPointValidityCache );
			if( existing != null )
				entryPointValidityCache = existing;
		}

		return entryPointValidityCache;
	}

	/**
	 * Enters the executable.
	 * 
	 * @param entryPointName
	 *        Name of entry point
	 * @param conversationService
	 *        The conversation service
	 * @return The result of the entry
	 * @throws ResourceException
	 * @see {@link Executable#enter(String, Object...)}
	 */
	private Object enter( String entryPointName, DelegatedResourceConversationService conversationService ) throws ResourceException
	{
		String documentName = getRequest().getResourceRef().getRemainingPart( true, false );
		documentName = attributes.validateDocumentName( documentName );

		ConcurrentMap<String, Boolean> entryPointValidityCache = null;

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, attributes.getDocumentSource(), false, attributes.getLanguageManager(), attributes.getDefaultLanguageTag(),
				attributes.isPrepare() );
			Executable executable = documentDescriptor.getDocument();

			if( executable.getEnterableExecutionContext() == null )
			{
				ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
				attributes.addLibraryLocations( executionContext );

				executionContext.getServices().put( attributes.getDocumentServiceName(), new DelegatedResourceDocumentService( this, documentDescriptor ) );
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
			entryPointValidityCache = getEntryPointValidityCache( executable );
			Boolean isValid = entryPointValidityCache.get( entryPointName );
			if( ( isValid != null ) && !isValid.booleanValue() )
				throw new NoSuchMethodException( entryPointName );

			// Enter!
			Object r = executable.enter( entryPointName, conversationService );

			return r;
		}
		catch( DocumentNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
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
			if( ConversationStoppedException.isConversationStopped( getRequest() ) )
			{
				getLogger().fine( "conversation.stop() was called" );
				return null;
			}
			else
				throw new ResourceException( x );
		}
		catch( NoSuchMethodException x )
		{
			// We are invalid
			if( entryPointValidityCache != null )
				entryPointValidityCache.put( entryPointName, false );

			throw new ResourceException( x );
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
}