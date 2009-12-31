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

import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource.DocumentDescriptor;

/**
 * Use <a href="http://alexgorbatchev.com/wiki/SyntaxHighlighter">Syntax
 * Highligher</a> to represent source code.
 * 
 * @author Tal Liron
 */
public class SyntaxHighlighterSourceRepresenter implements SourceRepresenter
{
	//
	// Attributes
	//

	/**
	 * @param context
	 * @return The base URL
	 */
	public String getBaseUrl( Context context )
	{
		if( baseUrl == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			baseUrl = (String) attributes.get( "com.threecrickets.prudence.util.SyntaxHighlighterSourceRepresenter.baseUrl" );

			if( baseUrl == null )
				baseUrl = "syntaxhighlighter/";
		}

		return baseUrl;
	}

	/**
	 * @param context
	 * @return The theme
	 */
	public String getTheme( Context context )
	{
		if( theme == null )
		{
			ConcurrentMap<String, Object> attributes = context.getAttributes();
			theme = (String) attributes.get( "com.threecrickets.prudence.util.SyntaxHighlighterSourceRepresenter.theme" );

			if( theme == null )
				theme = "Midnight";
		}

		return theme;
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public SyntaxHighlighterSourceRepresenter( Context context )
	{
		this.context = context;
	}

	//
	// SourceFormatter
	//

	public Representation representSource( String name, DocumentDescriptor<Document> documentDescriptor, Request request ) throws ResourceException
	{
		String tag = documentDescriptor.getTag();

		String brush = null, alias = tag;
		if( "py".equals( tag ) )
			brush = "Python";
		else if( "rb".equals( tag ) )
			brush = "Ruby";
		else if( "gv".equals( tag ) || "groovy".equals( tag ) )
		{
			brush = "Groovy";
			alias = "groovy";
		}
		else if( "js".equals( tag ) )
			brush = "JScript";
		else if( "clj".equals( tag ) )
			brush = "Clojure";
		else if( "php".equals( tag ) )
			brush = "Php";
		else if( "html".equals( tag ) )
			brush = "Xml";
		else if( "xhtml".equals( tag ) )
			brush = "Xml";
		else if( "xml".equals( tag ) )
			brush = "Xml";
		else if( "xslt".equals( tag ) )
			brush = "Xml";

		if( brush == null )
			return new StringRepresentation( documentDescriptor.getText() );

		String baseUrl = request.getRootRef().getPath() + getBaseUrl( context );
		String theme = getTheme( context );

		StringBuilder html = new StringBuilder();
		html.append( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" );
		html.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" );
		html.append( "  <head>\n" );
		html.append( "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" );
		html.append( "    <title>" + name + " - " + tag + "</title>\n" );
		html.append( "    <link href=\"" + baseUrl + "styles/shCore.css\" rel=\"stylesheet\" type=\"text/css\" />\n" );
		html.append( "    <link href=\"" + baseUrl + "styles/shTheme" + theme + ".css\" rel=\"stylesheet\" type=\"text/css\" />\n" );
		html.append( "    <script type=\"text/javascript\" src=\"" + baseUrl + "src/shCore.js\"></script>\n" );
		html.append( "    <script type=\"text/javascript\" src=\"" + baseUrl + "scripts/shBrush" + brush + ".js\"></script>\n" );
		html.append( "    <script type=\"text/javascript\">\n" );
		html.append( "      SyntaxHighlighter.config.clipboardSwf = '" + baseUrl + "scripts/clipboard.swf';" );
		html.append( "      SyntaxHighlighter.all();\n" );
		html.append( "    </script>\n" );
		html.append( "  </head>\n" );
		html.append( "  <body>\n" );
		html.append( "\n" );
		html.append( "    <noscript>" );
		html.append( "      You must enable JavaScript in your browser in order to see the source code." );
		html.append( "    </noscript>" );
		html.append( "<script type=\"syntaxhighlighter\" class=\"brush: " + alias + ";\"><![CDATA[" );
		html.append( documentDescriptor.getText() );
		html.append( "]]></script>\n" );
		html.append( "\n" );
		html.append( "  </body>\n" );
		html.append( "</html>\n" );

		Representation representation = new StringRepresentation( html );
		representation.setMediaType( MediaType.TEXT_HTML );
		return representation;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The context.
	 */
	private final Context context;

	/**
	 * The theme.
	 */
	private volatile String theme;

	/**
	 * The base URL.
	 */
	private volatile String baseUrl;
}
