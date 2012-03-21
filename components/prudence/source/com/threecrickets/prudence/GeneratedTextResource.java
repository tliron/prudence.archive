/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.GeneratedTextDeferredRepresentation;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.internal.attributes.GeneratedTextResourceAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ConversationStoppedException;
import com.threecrickets.prudence.service.GeneratedTextResourceConversationService;
import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;
import com.threecrickets.prudence.util.IoUtil;
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
 * A Restlet resource which executes a "text with scriptlets" Scripturian
 * {@link Executable} document for GET and POST verbs and redirects its standard
 * output to a {@link StringRepresentation}.
 * <p>
 * <code>document</code>, <code>application</code> and <code>conversation</code>
 * services are available as global variables in scriptlets. See
 * {@link GeneratedTextResourceDocumentService}, {@link ApplicationService} and
 * {@link GeneratedTextResourceConversationService}.
 * <p>
 * Before using this resource, make sure to configure a valid document source in
 * the application's {@link Context} as
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentSource</code.
 * This document source is exposed to scriptlets as <code>document.source</code>.
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
 * <code>com.threecrickets.prudence.cache:</code> {@link Cache}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.applicationServiceName</code>
 * : Defaults to "application".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.clientCachingMode:</code>
 * {@link Integer}, defaults to {@link #CLIENT_CACHING_MODE_CONDITIONAL}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.commonLibraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.conversationServiceName</code>
 * : Defaults to "conversation".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCacheKeyPattern:</code>
 * {@link String}, defaults to "{ri}|{dn}|{ptb}".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName:</code>
 * {@link String}, defaults to "index".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName:</code>
 * {@link String}, defaults to "default".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentFormatter:</code>
 * {@link DocumentFormatter}. Defaults to a {@link JygmentsDocumentFormatter}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentServiceName</code>
 * : Defaults to "document".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b></li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.executionController:</code>
 * {@link ExecutionController}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.fileUploadDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../uploads/".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.fileUploadSizeThreshold:</code>
 * {@link Integer}, defaults to zero.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.fragmentDirectory:</code>
 * {@link File}. Defaults to the {@link DocumentFileSource#getBasePath()} plus
 * "../fragments/".</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.libraryDocumentSource:</code>
 * {@link DocumentSource}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.prepare:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true.</li>
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

	/**
	 * Document name attribute for a {@link Request}.
	 */
	public static final String DOCUMENT_NAME_ATTRIBUTE = "com.threecrickets.prudence.GeneratedTextResource.documentName";

	/**
	 * Constant.
	 */
	public static final int CLIENT_CACHING_MODE_DISABLED = 0;

	/**
	 * Constant.
	 */
	public static final int CLIENT_CACHING_MODE_CONDITIONAL = 1;

	/**
	 * Constant.
	 */
	public static final int CLIENT_CACHING_MODE_OFFLINE = 2;

	/**
	 * Global list of supported encodings.
	 */
	public static final List<Encoding> SUPPORTED_ENCODINGS = new ArrayList<Encoding>();

	static
	{
		SUPPORTED_ENCODINGS.addAll( IoUtil.SUPPORTED_COMPRESSION_ENCODINGS );
		SUPPORTED_ENCODINGS.add( Encoding.IDENTITY );
	}

	//
	// Attributes
	//

	/**
	 * The attributes as configured in the {@link Application} context.
	 * 
	 * @return The attributes
	 */
	public GeneratedTextResourceAttributes getAttributes()
	{
		return attributes;
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

		Request request = getRequest();
		Map<String, Object> attributes = request.getAttributes();

		// Check for cached document name in the request
		String documentName = (String) attributes.get( DOCUMENT_NAME_ATTRIBUTE );
		if( documentName == null )
		{
			documentName = request.getResourceRef().getRemainingPart( true, false );
			documentName = this.attributes.validateDocumentName( documentName, this.attributes.getDefaultIncludedName() );
			attributes.put( DOCUMENT_NAME_ATTRIBUTE, documentName );
		}

		boolean isPassThrough = this.attributes.getPassThroughDocuments().contains( "/" + documentName );

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = this.attributes.getDocument( documentName, isPassThrough );

			// Media type is chosen according to the document descriptor tag
			MediaType mediaType = getMetadataService().getMediaType( documentDescriptor.getTag() );

			if( this.attributes.isNegotiateEncoding() )
			{
				// Add a variant for each supported encoding
				if( mediaType != null )
				{
					for( Encoding encoding : SUPPORTED_ENCODINGS )
					{
						Variant variant = new Variant( mediaType );
						variant.getEncodings().add( encoding );
						getVariants().add( variant );
					}
				}
				else
				{
					for( Encoding encoding : SUPPORTED_ENCODINGS )
					{
						Variant variant = new Variant();
						variant.getEncodings().add( encoding );
						getVariants().add( variant );
					}
				}
			}
			else if( mediaType != null )
				getVariants().add( new Variant( mediaType ) );
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

	@Override
	public Representation get() throws ResourceException
	{
		return generateText( null, null );
	}

	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		return generateText( null, variant );
	}

	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		return generateText( entity, null );
	}

	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		return generateText( entity, variant );
	}

	@Override
	public Representation put( Representation entity ) throws ResourceException
	{
		return generateText( entity, null );
	}

	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		return generateText( entity, variant );
	}

	@Override
	public Representation delete() throws ResourceException
	{
		return generateText( null, null );
	}

	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		return generateText( null, variant );
	}

	@Override
	public Representation options() throws ResourceException
	{
		return generateText( null, null );
	}

	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		return generateText( null, variant );
	}

	@Override
	public RepresentationInfo getInfo() throws ResourceException
	{
		return getInfo( null );
	}

	@Override
	public RepresentationInfo getInfo( Variant variant ) throws ResourceException
	{
		Request request = getRequest();
		Map<String, Object> attributes = request.getAttributes();

		// Check for cached document name in the request
		String documentName = (String) attributes.get( DOCUMENT_NAME_ATTRIBUTE );
		if( documentName == null )
		{
			documentName = request.getResourceRef().getRemainingPart( true, false );
			documentName = this.attributes.validateDocumentName( documentName, this.attributes.getDefaultIncludedName() );
			attributes.put( DOCUMENT_NAME_ATTRIBUTE, documentName );
		}

		boolean isPassThrough = this.attributes.getPassThroughDocuments().contains( "/" + documentName );

		GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this, null, null, variant );
		try
		{
			CacheEntry cacheEntry = documentService.getCacheEntry( documentName, isPassThrough );
			if( cacheEntry != null )
				return cacheEntry.getInfo();
			else
				return get( variant );
		}
		catch( ParsingException x )
		{
			throw new ResourceException( x );
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
	 * The attributes as configured in the {@link Application} context.
	 */
	private final GeneratedTextResourceAttributes attributes = new GeneratedTextResourceAttributes( this );

	/**
	 * Flag for asynchronous support (experimental).
	 */
	private final boolean asynchronousSupport = false;

	/**
	 * Generates and possibly caches a textual representation. The returned
	 * representation is either a {@link StringRepresentation} or a
	 * {@link GeneratedTextDeferredRepresentation}. Text in the former case
	 * could be the result of either execution or retrieval from the cache.
	 * 
	 * @param entity
	 *        The entity
	 * @param variant
	 *        The variant
	 * @return A representation
	 * @throws ResourceException
	 */
	private Representation generateText( Representation entity, Variant variant ) throws ResourceException
	{
		Request request = getRequest();
		Map<String, Object> attributes = request.getAttributes();

		// Check for cached document name in the request
		String documentName = (String) attributes.get( DOCUMENT_NAME_ATTRIBUTE );
		if( documentName == null )
		{
			documentName = request.getResourceRef().getRemainingPart( true, false );
			documentName = this.attributes.validateDocumentName( documentName, this.attributes.getDefaultIncludedName() );
			attributes.put( DOCUMENT_NAME_ATTRIBUTE, documentName );
		}

		boolean isPassThrough = this.attributes.getPassThroughDocuments().contains( "/" + documentName );

		try
		{
			if( this.attributes.isSourceViewable() )
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

					DocumentDescriptor<Executable> documentDescriptor = this.attributes.getDocumentSource().getDocument( documentName );
					DocumentFormatter<Executable> documentFormatter = this.attributes.getDocumentFormatter();
					if( documentFormatter != null )
						return new StringRepresentation( documentFormatter.format( documentDescriptor, documentName, lineNumber ), MediaType.TEXT_HTML );
					else
						return new StringRepresentation( documentDescriptor.getSourceCode() );
				}
			}

			ExecutionContext executionContext = new ExecutionContext();
			this.attributes.addLibraryLocations( executionContext );

			GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this, executionContext, entity, variant );
			Representation representation = null;
			try
			{
				// Execute and represent output
				representation = documentService.include( documentName, isPassThrough );

				List<CacheDirective> cacheDirectives = getResponse().getCacheDirectives();
				switch( this.attributes.getClientCachingMode() )
				{
					case CLIENT_CACHING_MODE_DISABLED:
					{
						// Remove all conditional and caching headers,
						// explicitly setting "no-cache"
						representation.setModificationDate( null );
						representation.setExpirationDate( null );
						representation.setTag( null );
						cacheDirectives.clear();
						cacheDirectives.add( CacheDirective.noCache() );
						break;
					}

					case CLIENT_CACHING_MODE_CONDITIONAL:
						// Leave conditional headers intact, but remove cache
						// headers, explicitly setting "no-cache"
						representation.setExpirationDate( null );
						cacheDirectives.clear();
						cacheDirectives.add( CacheDirective.noCache() );
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
				if( ConversationStoppedException.isConversationStopped( getRequest() ) )
				{
					getLogger().fine( "conversation.stop() was called" );
					return null;
				}

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
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
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