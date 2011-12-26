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

package com.threecrickets.prudence.util;

import java.util.Collection;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Product;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.representation.StringRepresentation;

import com.threecrickets.prudence.DelegatedStatusService;
import com.threecrickets.prudence.SourceCodeResource;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.exception.PreparationException;
import com.threecrickets.scripturian.exception.StackFrame;

/**
 * An HTML representation of lots of Prudence and Restlet state useful for
 * debugging.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 * @see DelegatedStatusService
 */
public class DebugRepresentation extends StringRepresentation
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param status
	 *        The status
	 * @param request
	 *        The request
	 * @param response
	 *        The response
	 */
	public DebugRepresentation( Status status, Request request, Response response, String sourceCodeUri )
	{
		super( null, MediaType.TEXT_HTML );

		Throwable throwable = status.getThrowable();
		ClientInfo clientInfo = request.getClientInfo();
		Conditions conditions = request.getConditions();

		StringBuilder html = new StringBuilder();

		html.append( "<html>\n" );
		html.append( "<head>\n" );
		html.append( "<title>Prudence Debug Page</title>\n" );
		html.append( "<style>\n" );
		html.append( "body { font-family: sans-serif; }\n" );
		html.append( ".name { font-weight: bold; font-style: italic; }\n" );
		html.append( "</style>\n" );
		html.append( "</head>\n" );
		html.append( "<body\n" );

		html.append( "<h2>Prudence Debug Page</h2>" );

		Iterable<StackFrame> stack = null;

		if( throwable instanceof ExecutionException )
		{
			html.append( "<h3>Scripturian Execution Error</h3>" );
			ExecutionException executionException = (ExecutionException) throwable;
			stack = executionException.getStack();
		}
		else if( throwable instanceof PreparationException )
		{
			html.append( "<h3>Scripturian Preparation Error</h3>" );
			PreparationException preparationException = (PreparationException) throwable;
			stack = preparationException.getStack();
		}
		else if( throwable instanceof ParsingException )
		{
			html.append( "<h3>Scripturian Parsing Error</h3>" );
			ParsingException parsingException = (ParsingException) throwable;
			stack = parsingException.getStack();
		}

		html.append( "<h3>" );
		appendSafe( html, throwable.getMessage() );
		html.append( "</h3>" );

		if( stack != null )
		{
			html.append( "<div id=\"error\">" );
			for( StackFrame stackFrame : stack )
			{
				int lineNumber = stackFrame.getLineNumber();
				appendName( html, "At" );
				if( ( sourceCodeUri != null ) && sourceCodeUri.length() > 0 )
				{
					html.append( "<a href=\"" );
					html.append( request.getRootRef() );
					html.append( sourceCodeUri );
					html.append( '?' );
					html.append( SourceCodeResource.DOCUMENT );
					html.append( '=' );
					html.append( Reference.encode( stackFrame.getDocumentName() ) );
					if( lineNumber >= 0 )
					{
						html.append( '&' );
						html.append( SourceCodeResource.HIGHLIGHT );
						html.append( '=' );
						html.append( Reference.encode( String.valueOf( lineNumber ) ) );
					}
					html.append( "\">" );
				}
				appendSafe( html, stackFrame.getDocumentName() );
				if( stackFrame.getLineNumber() >= 0 )
				{
					html.append( " @ " );
					html.append( stackFrame.getLineNumber() );
				}
				if( stackFrame.getColumnNumber() >= 0 )
				{
					html.append( "," );
					html.append( stackFrame.getColumnNumber() );
				}
				if( ( sourceCodeUri != null ) && sourceCodeUri.length() > 0 )
					html.append( "</a>" );
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		html.append( "<h3>References</h3>" );
		html.append( "<div id=\"references\">" );
		appendName( html, "Resource" );
		appendValue( html, request.getResourceRef() );
		appendName( html, "Original" );
		appendValue( html, request.getOriginalRef() );
		appendName( html, "Root" );
		appendValue( html, request.getRootRef() );
		if( request.getReferrerRef() != null )
		{
			appendName( html, "Referrer" );
			appendValue( html, request.getReferrerRef() );
		}
		appendName( html, "Host" );
		appendValue( html, request.getHostRef() );
		Reference captiveReference = CapturingRedirector.getCapturedReference( request );
		if( captiveReference != null )
		{
			appendName( html, "Captive" );
			appendValue( html, captiveReference );
		}
		html.append( "</div>" );

		Form form = request.getResourceRef().getQueryAsForm();
		if( !form.isEmpty() )
		{
			html.append( "<h3>Query</h3>" );
			html.append( "<div id=\"query\">" );
			for( Map.Entry<String, String> entry : form.getValuesMap().entrySet() )
			{
				appendName( html, entry.getKey() );
				appendValue( html, entry.getValue() );
			}
			html.append( "</div>" );
		}

		if( !request.getCookies().isEmpty() )
		{
			html.append( "<h3>Cookies</h3>" );
			html.append( "<div id=\"cookies\">" );
			for( Cookie cookie : request.getCookies() )
			{
				appendName( html, cookie.getName() );
				if( cookie.getDomain() != null )
					appendValue( html, cookie.getValue(), " (", cookie.getVersion(), ") for ", cookie.getDomain(), " ", cookie.getPath() );
				else
					appendValue( html, cookie.getValue(), " (", cookie.getVersion(), ")" );
			}
			html.append( "</div>" );
		}

		html.append( "<h3>Request</h3>" );
		html.append( "<div id=\"request\">" );
		appendName( html, "Time" );
		appendValue( html, request.getDate() );
		appendName( html, "Method" );
		appendValue( html, request.getMethod() );
		appendName( html, "Protocol" );
		appendValue( html, request.getProtocol() );
		appendName( html, "Media Types" );
		appendValue( html, clientInfo.getAcceptedMediaTypes() );
		appendName( html, "Encodings" );
		appendValue( html, clientInfo.getAcceptedEncodings() );
		appendName( html, "Character Sets" );
		appendValue( html, clientInfo.getAcceptedCharacterSets() );
		appendName( html, "Languages" );
		appendValue( html, clientInfo.getAcceptedLanguages() );
		html.append( "</div>" );

		if( conditions.hasSome() )
		{
			html.append( "<h3>Conditions</h3>" );
			html.append( "<div id=\"conditions\">" );
			if( conditions.getModifiedSince() != null )
			{
				appendName( html, "Modified Since" );
				appendValue( html, conditions.getModifiedSince() );
			}
			if( conditions.getUnmodifiedSince() != null )
			{
				appendName( html, "Unmodified Since" );
				appendValue( html, conditions.getUnmodifiedSince() );
			}
			if( conditions.getRangeDate() != null )
			{
				appendName( html, "Range Date" );
				appendValue( html, conditions.getRangeDate() );
			}
			if( !conditions.getMatch().isEmpty() )
			{
				appendName( html, "Match Tags" );
				appendValue( html, conditions.getMatch() );
			}
			if( !conditions.getNoneMatch().isEmpty() )
			{
				appendName( html, "None-Match Tags" );
				appendValue( html, conditions.getNoneMatch() );
			}
			if( conditions.getRangeTag() != null )
			{
				appendName( html, "Range Tag" );
				appendValue( html, conditions.getRangeTag() );
			}
			html.append( "</div>" );
		}

		if( request.isEntityAvailable() )
		{
			html.append( "<h3>Entity</h3>" );
			html.append( "<div id=\"entity\">" );
			appendSafe( html, request.getEntity() );
			html.append( "</div>" );
		}

		if( !request.getCacheDirectives().isEmpty() )
		{
			boolean has = false;
			for( CacheDirective cacheDirective : request.getCacheDirectives() )
			{
				if( ( cacheDirective.getValue() != null ) && ( cacheDirective.getValue().length() > 0 ) )
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
					if( ( cacheDirective.getValue() != null ) && ( cacheDirective.getValue().length() > 0 ) )
					{
						appendName( html, cacheDirective.getName() );
						appendValue( html, cacheDirective.getValue() );
					}
				}
				html.append( "</div>" );
			}
		}

		html.append( "<h3>Client</h3>" );
		html.append( "<div id=\"client\">" );
		appendName( html, "Address" );
		appendValue( html, clientInfo.getAddress(), " port ", clientInfo.getPort() );
		appendName( html, "Upstream Address" );
		appendValue( html, clientInfo.getUpstreamAddress() );
		if( !clientInfo.getForwardedAddresses().isEmpty() )
		{
			appendName( html, "Forwarded Addresses" );
			appendValue( html, clientInfo.getForwardedAddresses() );
		}
		appendName( html, "Agent" );
		appendValue( html, clientInfo.getAgentName(), " ", clientInfo.getAgentVersion() );
		html.append( "<br />" );
		appendName( html, "Products" );
		html.append( "<br />" );
		for( Product product : clientInfo.getAgentProducts() )
		{
			html.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
			if( product.getComment() != null )
				appendValue( html, product.getName(), " ", product.getVersion(), " (", product.getComment(), ")" );
			else
				appendValue( html, product.getName(), " ", product.getVersion() );
		}
		if( clientInfo.getFrom() != null )
		{
			appendName( html, "From" );
			appendValue( html, clientInfo.getFrom() );
		}
		html.append( "</div>" );

		if( !request.getAttributes().isEmpty() )
		{
			html.append( "<h3>Attributes</h3>" );
			html.append( "<div id=\"attributes\">" );
			for( Map.Entry<String, Object> attribute : request.getAttributes().entrySet() )
			{
				appendName( html, attribute.getKey() );
				if( attribute.getValue() instanceof Collection<?> )
				{
					html.append( "<br />" );
					for( Object o : (Collection<?>) attribute.getValue() )
					{
						html.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
						if( o instanceof Parameter )
						{
							Parameter parameter = (Parameter) o;
							appendValue( html, parameter.getName(), " = ", parameter.getValue() );
						}
						else
							appendValue( html, o );
					}
				}
				else
				{
					appendValue( html, attribute.getValue() );
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
				appendName( html, warning.getDate() );
				appendValue( html, warning.getText(), " from ", warning.getAgent(), " (", warning.getStatus(), ")" );
			}
			html.append( "</div>" );
		}

		Application application = Application.getCurrent();
		if( application != null )
		{
			String name = application.getName();
			if( name != null )
				html.append( "<h3>Application: " + name + "</h3>" );
			else
				html.append( "<h3>Application</h3>" );

			for( Map.Entry<String, Object> attribute : application.getContext().getAttributes().entrySet() )
			{
				appendName( html, attribute.getKey() );
				if( attribute.getValue() instanceof Collection<?> )
				{
					html.append( "<br />" );
					for( Object o : (Collection<?>) attribute.getValue() )
					{
						html.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" );
						appendValue( html, o );
					}
				}
				else
					appendValue( html, attribute.getValue() );
			}
		}

		if( throwable.getStackTrace() != null )
		{
			html.append( "<h3>Machine Stack Trace</h3>" );
			html.append( "<div id=\"stack-trace\">" );
			html.append( "<h4>" );
			appendSafe( html, throwable.getClass().getCanonicalName() );
			html.append( "</h4>" );
			for( StackTraceElement stackTraceElement : throwable.getStackTrace() )
			{
				appendSafe( html, stackTraceElement.getClassName() );
				html.append( '.' );
				appendSafe( html, stackTraceElement.getMethodName() );
				if( stackTraceElement.getFileName() != null )
				{
					html.append( " (" );
					appendSafe( html, stackTraceElement.getFileName() );
					html.append( ':' );
					html.append( stackTraceElement.getLineNumber() );
					html.append( ')' );
				}
				html.append( "<br />" );
			}
			html.append( "</div>" );
		}

		html.append( "</body>\n" );
		html.append( "</html>\n" );

		setText( html.toString() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Escapes HTML.
	 * 
	 * @param html
	 *        The HTML builder
	 * @param string
	 *        Value to append
	 */
	private static void appendSafe( StringBuilder html, Object string )
	{
		if( string != null )
			html.append( string.toString().replace( "<", "&lt;" ).replace( ">", "&gt;" ) );
	}

	/**
	 * A span with CSS class "name".
	 * 
	 * @param html
	 *        The HTML builder
	 * @param string
	 *        Value to append
	 */
	private static void appendName( StringBuilder html, Object string )
	{
		html.append( "<span class=\"name\">" );
		appendSafe( html, string );
		html.append( ":</span> " );
	}

	/**
	 * A span with CSS class "value".
	 * 
	 * @param html
	 *        The HTML builder
	 * @param strings
	 *        Values to append
	 */
	private static void appendValue( StringBuilder html, Object... strings )
	{
		html.append( "<span class=\"value\">" );
		for( Object string : strings )
			appendSafe( html, string );
		html.append( "</span><br />" );
	}
}
