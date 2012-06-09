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

package com.threecrickets.prudence.util;

import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;
import com.threecrickets.scripturian.LanguageAdapter;
import com.threecrickets.scripturian.ScriptletPlugin;
import com.threecrickets.scripturian.internal.ScripturianUtil;

/**
 * A {@link ScriptletPlugin} that supports a few special tags:
 * <ul>
 * <li><b>==</b>: outputs a conversation.local with the context as the name</li>
 * <li><b>{{</b>: calls
 * {@link GeneratedTextResourceDocumentService#startCapture(String)} with the
 * content as the name argument</li>
 * <li><b>}}</b>: calls
 * {@link GeneratedTextResourceDocumentService#endCapture()}</li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class PrudenceScriptletPlugin implements ScriptletPlugin
{
	//
	// ScriptletPlugin
	//

	public String getScriptlet( String code, LanguageAdapter languageAdapter, String content )
	{
		if( "==".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			if( JAVASCRIPT.equals( language ) )
				return "print(conversation.locals.get(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ")||\"\");";
			else if( PYTHON.equals( language ) )
				return "sys.stdout.write(conversation.locals.get(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ") or \"\");";
			else if( RUBY.equals( language ) )
				return "print($conversation.locals.get(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ")||\"\");";
			else if( GROOVY.equals( language ) )
				return "print(conversation.locals.get(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ")||\"\");";
			else if( CLOJURE.equals( language ) )
				return "(print (or (.. conversation getLocals (get " + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ")) \"\"))";
			else if( PHP.equals( language ) )
				return "print($conversation->locals->get(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ")||\"\");";
		}
		else if( "{{".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			if( JAVASCRIPT.equals( language ) )
				return "document.startCapture(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ");";
			else if( PYTHON.equals( language ) )
				return "document.startCapture(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ");";
			else if( RUBY.equals( language ) )
				return "$document.start_capture(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ");";
			else if( GROOVY.equals( language ) )
				return "document.startCapture(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ");";
			else if( CLOJURE.equals( language ) )
				return "(.startCapture document " + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ")";
			else if( PHP.equals( language ) )
				return "$document->startCapture(" + ScripturianUtil.doubleQuotedLiteral( content.trim() ) + ");";
		}
		else if( "}}".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			if( JAVASCRIPT.equals( language ) )
				return "document.endCapture();";
			else if( PYTHON.equals( language ) )
				return "document.endCapture();";
			else if( RUBY.equals( language ) )
				return "$document.end_capture();";
			else if( GROOVY.equals( language ) )
				return "document.endCapture();";
			else if( CLOJURE.equals( language ) )
				return "(.endCapture document)";
			else if( PHP.equals( language ) )
				return "$document->endCapture();";
		}

		return "";
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String JAVASCRIPT = "JavaScript";

	private static final String PYTHON = "Python";

	private static final String RUBY = "Ruby";

	private static final String GROOVY = "Groovy";

	private static final String CLOJURE = "Clojure";

	private static final String PHP = "PHP";
}
