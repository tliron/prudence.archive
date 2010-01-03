/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Product;
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Redirector;
import org.restlet.service.StatusService;

import com.threecrickets.scripturian.exception.DocumentRunException;

/**
 * Allows delegating the handling of errors to specified restlets.
 * 
 * @author Tal Liron
 */
public class DelegatedStatusService extends StatusService
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public DelegatedStatusService()
	{
		super();
		setOverwriting( true );
	}

	/**
	 * Constructor.
	 * 
	 * @param enabled
	 *        True to enable the service
	 */
	public DelegatedStatusService( boolean enabled )
	{
		super( enabled );
		setOverwriting( true );
	}

	//
	// Attributes
	//

	/**
	 * A map of error statuses to target restlets. If no handler is mapped for a
	 * status, the default handling will kick in. (Modifiable by concurrent
	 * threads.)
	 * 
	 * @return The error handlers
	 */
	public ConcurrentMap<Integer, Restlet> getErrorHandlers()
	{
		return errorHandlers;
	}

	/**
	 * Whether we should show debugging information on errors.
	 * 
	 * @return True if debugging
	 * @see #setDebugging(boolean)
	 */
	public boolean isDebugging()
	{
		return isDebugging;
	}

	/**
	 * Whether we should show debugging information on errors.
	 * 
	 * @param isDebugging
	 *        True if debugging
	 * @see #isDebugging()
	 */
	public void setDebugging( boolean isDebugging )
	{
		this.isDebugging = isDebugging;
	}

	//
	// Operations
	//

	/**
	 * Sets the handler for an error status.
	 * 
	 * @param status
	 *        The status code
	 * @param errorHandler
	 *        The error handler
	 */
	public void setHandler( int status, Restlet errorHandler )
	{
		errorHandlers.put( status, errorHandler );
	}

	/**
	 * Sets the handler for an error status to be a {@link Redirector} with mode
	 * {@link Redirector#MODE_SERVER_DISPATCHER}.
	 * 
	 * @param status
	 *        The status code
	 * @param targetPattern
	 *        The URI pattern
	 * @param context
	 *        The context
	 */
	public void redirect( int status, String targetPattern, Context context )
	{
		setHandler( status, new Redirector( context, targetPattern, Redirector.MODE_SERVER_DISPATCHER ) );
	}

	/**
	 * Removes the handler for an error status.
	 * 
	 * @param status
	 *        The status code
	 */
	public void removeHandler( int status )
	{
		errorHandlers.remove( status );
	}

	//
	// StatusService
	//

	@Override
	public Representation getRepresentation( Status status, Request request, Response response )
	{
		if( isEnabled() )
		{
			Restlet errorHandler = errorHandlers.get( status.getCode() );

			if( errorHandler != null )
			{
				// Clear the status
				response.setStatus( Status.SUCCESS_OK );

				// Delegate
				errorHandler.handle( request, response );

				// Return the status
				response.setStatus( status );

				Representation representation = response.getEntity();

				// Avoid caching, which could require other interchanges
				// with client that we can't handle from here
				representation.setExpirationDate( null );
				representation.setModificationDate( null );
				representation.setTag( null );

				return representation;
			}

			if( isDebugging() && ( status.getThrowable() != null ) )
				return getDebugRepresentation( status, request, response );
		}

		return super.getRepresentation( status, request, response );
	}

	public Representation getDebugRepresentation( Status status, Request request, Response response )
	{
		Throwable throwable = status.getThrowable();
		ClientInfo clientInfo = request.getClientInfo();
		Conditions conditions = request.getConditions();

		StringBuilder html = new StringBuilder();

		html.append( "<html>\n" );
		html.append( "<head>\n" );
		html.append( "   <title>Prudence - Debug</title>\n" );
		html.append( "</head>\n" );
		html.append( "<body style=\"font-family: sans-serif;\">\n" );

		html.append( "<h3>" );
		html.append( throwable.getMessage().replace( "<", "&lt;" ).replace( ">", "&gt;" ) );
		html.append( "</h3>" );

		if( throwable instanceof DocumentRunException )
		{
			DocumentRunException documentRunException = (DocumentRunException) throwable;
			html.append( "<div id=\"error\">" );
			html.append( "Document: " );
			html.append( documentRunException.getDocumentName() );
			html.append( " <a href=\"" );
			html.append( request.getResourceRef() );
			html.append( "?source=true\">" );
			html.append( "(source)</a>" );
			html.append( "<br />" );
			if( documentRunException.getLineNumber() >= 0 )
			{
				html.append( "Line: " );
				html.append( documentRunException.getLineNumber() );
				html.append( "<br />" );
			}
			if( documentRunException.getColumnNumber() >= 0 )
			{
				html.append( "Column: " );
				html.append( documentRunException.getColumnNumber() );
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		html.append( "<h3>References</h3>" );
		html.append( "<div id=\"references\">" );
		html.append( "Resource: " );
		html.append( request.getResourceRef() );
		html.append( "<br />" );
		html.append( "Original: " );
		html.append( request.getOriginalRef() );
		html.append( "<br />" );
		html.append( "Root: " );
		html.append( request.getRootRef() );
		html.append( "<br />" );
		if( request.getReferrerRef() != null )
		{
			html.append( "Referrer: " );
			html.append( request.getReferrerRef() );
			html.append( "<br />" );
		}
		html.append( "Host: " );
		html.append( request.getHostRef() );
		html.append( "<br />" );
		html.append( "</div>" );

		Form form = request.getResourceRef().getQueryAsForm();
		if( !form.isEmpty() )
		{
			html.append( "<h3>Query</h3>" );
			html.append( "<div id=\"query\">" );
			for( Map.Entry<String, String> entry : form.getValuesMap().entrySet() )
			{
				html.append( entry.getKey() );
				html.append( ": " );
				html.append( entry.getValue() );
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		if( !request.getCookies().isEmpty() )
		{
			html.append( "<h3>Cookies</h3>" );
			html.append( "<div id=\"cookies\">" );
			for( Cookie cookie : request.getCookies() )
			{
				html.append( cookie.getName() );
				html.append( ": " );
				html.append( cookie.getValue() );
				html.append( " (" );
				html.append( cookie.getVersion() );
				html.append( ") for " );
				html.append( cookie.getDomain() );
				html.append( " " );
				html.append( cookie.getPath() );
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		html.append( "<h3>Request</h3>" );
		html.append( "<div id=\"request\">" );
		html.append( request.getDate() );
		html.append( "<br />" );
		html.append( "Method: " );
		html.append( request.getMethod() );
		html.append( "<br />" );
		html.append( "Protocol: " );
		html.append( request.getProtocol() );
		html.append( "<br />" );
		html.append( "Media Types: " );
		html.append( clientInfo.getAcceptedMediaTypes() );
		html.append( "<br />" );
		html.append( "Encodings: " );
		html.append( clientInfo.getAcceptedEncodings() );
		html.append( "<br />" );
		html.append( "Character Sets: " );
		html.append( clientInfo.getAcceptedCharacterSets() );
		html.append( "<br />" );
		html.append( "Languages: " );
		html.append( clientInfo.getAcceptedLanguages() );
		html.append( "<br />" );
		html.append( "</div>" );

		if( conditions.hasSome() )
		{
			html.append( "<h3>Conditions</h3>" );
			html.append( "<div id=\"conditions\">" );
			if( conditions.getModifiedSince() != null )
			{
				html.append( "Modified Since: " );
				html.append( conditions.getModifiedSince() );
				html.append( "<br />" );
			}
			if( conditions.getUnmodifiedSince() != null )
			{
				html.append( "Unmodified Since: " );
				html.append( conditions.getUnmodifiedSince() );
				html.append( "<br />" );
			}
			if( conditions.getRangeDate() != null )
			{
				html.append( "Range Date: " );
				html.append( conditions.getRangeDate() );
				html.append( "<br />" );
			}
			if( !conditions.getMatch().isEmpty() )
			{
				html.append( "Match tTgs: " );
				html.append( conditions.getMatch() );
				html.append( "<br />" );
			}
			if( !conditions.getNoneMatch().isEmpty() )
			{
				html.append( "None-Match Tags: " );
				html.append( conditions.getNoneMatch() );
				html.append( "<br />" );
			}
			if( conditions.getRangeTag() != null )
			{
				html.append( "Range Tag: " );
				html.append( conditions.getRangeTag() );
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		if( request.isEntityAvailable() )
		{
			html.append( "<h3>Entity</h3>" );
			html.append( "<div id=\"entity\">" );
			html.append( request.getEntity() );
			html.append( "</div>" );
		}

		if( !request.getCacheDirectives().isEmpty() )
		{
			boolean has = false;
			for( CacheDirective cacheDirective : request.getCacheDirectives() )
			{
				if( cacheDirective.getValue().length() > 0 )
				{
					has = true;
					break;
				}
			}
			if( has )
			{
				html.append( "<h3>Cache Directives</h3>" );
				html.append( "<div id=\"cache-directives\">" );
				for( CacheDirective cacheDirective : request.getCacheDirectives() )
				{
					if( cacheDirective.getValue().length() > 0 )
					{
						html.append( cacheDirective.getName() );
						html.append( ": " );
						html.append( cacheDirective.getValue() );
						html.append( "<br />" );
					}
				}
				html.append( "</div>" );
			}
		}

		html.append( "<h3>Client</h3>" );
		html.append( "<div id=\"client\">" );
		html.append( "Address: " );
		html.append( clientInfo.getAddress() );
		html.append( " port " );
		html.append( clientInfo.getPort() );
		html.append( "<br />" );
		html.append( "Upstream Address: " );
		html.append( clientInfo.getUpstreamAddress() );
		html.append( "<br />" );
		if( !clientInfo.getForwardedAddresses().isEmpty() )
		{
			html.append( "Forwarded Addresses: " );
			html.append( clientInfo.getForwardedAddresses() );
			html.append( "<br />" );
		}
		html.append( "Agent: " );
		html.append( clientInfo.getAgentName() );
		html.append( " " );
		html.append( clientInfo.getAgentVersion() );
		html.append( "<br />" );
		html.append( "Products:<br />" );
		for( Product product : clientInfo.getAgentProducts() )
		{
			html.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
			html.append( product.getName() );
			html.append( " " );
			html.append( product.getVersion() );
			if( product.getComment() != null )
			{
				html.append( " (" );
				html.append( product.getComment() );
				html.append( ")" );
			}
			html.append( "<br />" );
		}
		if( clientInfo.getFrom() != null )
		{
			html.append( "From: " );
			html.append( clientInfo.getFrom() );
			html.append( "<br />" );
		}
		html.append( "</div>" );

		if( !request.getAttributes().isEmpty() )
		{
			html.append( "<h3>Attributes</h3>" );
			html.append( "<div id=\"attributes\">" );
			for( Map.Entry<String, Object> attribute : request.getAttributes().entrySet() )
			{
				html.append( attribute.getKey() );
				html.append( ": " );
				if( attribute.getValue() instanceof Collection<?> )
				{
					html.append( "<br />" );
					for( Object o : (Collection<?>) attribute.getValue() )
					{
						html.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
						html.append( o );
						html.append( "<br />" );
					}
				}
				else
				{
					html.append( attribute.getValue() );
					html.append( "<br />" );
				}
			}
			html.append( "</div>" );
		}

		if( !request.getWarnings().isEmpty() )
		{
			html.append( "<h3>Warnings</h3>" );
			html.append( "<div id=\"warnings\">" );
			for( Warning warning : request.getWarnings() )
			{
				html.append( warning.getDate() );
				html.append( ": " );
				html.append( warning.getText() );
				html.append( ", from " );
				html.append( warning.getAgent() );
				html.append( " (" );
				html.append( warning.getStatus() );
				html.append( ")" );
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		html.append( "<h3>Stack Trace</h3>" );

		html.append( "<div id=\"stack-trace\">" );
		html.append( "<h4>" );
		html.append( throwable.getClass().getCanonicalName() );
		html.append( "</h4>" );
		for( StackTraceElement stackTraceElement : throwable.getStackTrace() )
		{
			html.append( stackTraceElement.getClassName() );
			html.append( '.' );
			html.append( stackTraceElement.getMethodName() );
			html.append( " (" );
			html.append( stackTraceElement.getFileName() );
			html.append( ':' );
			html.append( stackTraceElement.getLineNumber() );
			html.append( ')' );
			html.append( "<br />" );
		}
		html.append( "</div>" );

		html.append( "</body>\n" );
		html.append( "</html>\n" );

		return new StringRepresentation( html.toString(), MediaType.TEXT_HTML );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Our map of status codes to error handlers.
	 */
	private final ConcurrentMap<Integer, Restlet> errorHandlers = new ConcurrentHashMap<Integer, Restlet>();

	/**
	 * Whether we are debugging.
	 */
	private volatile boolean isDebugging = true;
}
