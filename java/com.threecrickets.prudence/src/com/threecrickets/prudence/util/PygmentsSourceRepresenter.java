package com.threecrickets.prudence.util;

import java.io.StringReader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.internal.ExposedContainerForPygmentsSourceRepresenter;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource.DocumentDescriptor;

/**
 * Use <a href="http://pygments.org/">Pygments</a> over Jython to represent
 * source code.
 * 
 * @author Tal Liron
 */
public class PygmentsSourceRepresenter implements SourceRepresenter
{
	//
	// SourceFormatter
	//

	public Representation representSource( String name, int lineNumber, DocumentDescriptor<Document> documentDescriptor, Request request ) throws ResourceException
	{
		String tag = documentDescriptor.getTag();
		String language = null;
		if( "py".equals( tag ) )
			language = "python";
		else if( "rb".equals( tag ) )
			language = "ruby";
		else if( "gv".equals( tag ) || "groovy".equals( tag ) )
			language = "groovy";
		else if( "js".equals( tag ) )
			language = "javascript";
		else if( "clj".equals( tag ) )
			language = "clojure";
		else if( "php".equals( tag ) )
			language = "html+php";
		else if( "html".equals( tag ) )
			language = "html";
		else if( "xhtml".equals( tag ) )
			language = "html";
		else if( "xml".equals( tag ) )
			language = "xml";
		else if( "xslt".equals( tag ) )
			language = "xslt";

		if( language == null )
			return new StringRepresentation( documentDescriptor.getText() );

		ExposedContainerForPygmentsSourceRepresenter container = new ExposedContainerForPygmentsSourceRepresenter( documentDescriptor.getText(), lineNumber, language, name, "vs", "#dddddd", "#dddd00" );
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName( "python" );
		ScriptContext scriptContext = scriptEngine.getContext();
		scriptContext.setAttribute( "container", container, ScriptContext.ENGINE_SCOPE );

		if( scriptEngine instanceof Compilable )
		{
			synchronized( program )
			{
				if( compiledScript == null )
				{
					try
					{
						compiledScript = ( (Compilable) scriptEngine ).compile( program );
					}
					catch( ScriptException x )
					{
						throw new ResourceException( x );
					}
				}

				try
				{
					compiledScript.eval( scriptContext );
				}
				catch( ScriptException x )
				{
					throw new ResourceException( x );
				}
			}
		}
		else
		{
			try
			{
				scriptEngine.eval( new StringReader( program ) );
			}
			catch( ScriptException x )
			{
				throw new ResourceException( x );
				// return new StringRepresentation( documentDescriptor.getText()
				// );
			}
		}

		Representation representation = new StringRepresentation( container.getText() );
		representation.setMediaType( MediaType.TEXT_HTML );
		return representation;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private CompiledScript compiledScript;

	private final String program = "from pygments import highlight\n" + "from pygments.lexers import get_lexer_by_name\n" + "from pygments.formatters import HtmlFormatter\n"
		+ "lexer = get_lexer_by_name(container.language, stripall=True)\n" + "formatter = HtmlFormatter(full=True, linenos='table', hl_lines=(container.lineNumber,), title=container.title, style=container.style)\n"
		+ "formatter.style.background_color = container.background\n" + "formatter.style.highlight_color = container.highlight\n" + "container.text = highlight(container.text, lexer, formatter)\n";
}
