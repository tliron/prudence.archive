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

package com.threecrickets.prudence;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.internal.ExposedContainerForGeneratedTextResource;
import com.threecrickets.prudence.internal.PrudenceUtils;
import com.threecrickets.prudence.internal.RepresentableString;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.ScriptletController;

/**
 * A Restlet resource which runs a Scripturian {@link Document} for HTTP GET and
 * POST verbs and redirects its standard output to a
 * {@link StringRepresentation}.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getDocumentSource()}. This source
 * is accessible from scriptlets, via <code>document.container.source</code>.
 * <p>
 * This resource supports two modes of output:
 * <ul>
 * <li>Caching mode: First, the entire document is run, with its output sent
 * into a buffer. This buffer is then cached, and <i>only then</i> sent to the
 * client. This is the default mode and recommended for most documents.
 * Scriptlets can control the duration of their individual cache by changing the
 * value of <code>document.cacheDuration</code> (see {@link Document}). Because
 * output is not sent to the client until after the script finished its run, it
 * is possible for scriptlets to determine output characteristics at any time by
 * changing the values of <code>document.container.mediaType</code>,
 * <code>document.container.characterSet</code>, and
 * <code>document.container.language</code> (see below).</li>
 * <li>Streaming mode: Output is sent to the client <i>while</i> scriptlets run.
 * This is recommended for documents that need to output a very large amount of
 * text, which might take a long time, or that might otherwise encounter
 * slow-downs while running. In either case, you want the client to receive
 * ongoing output. The output of the document is not cached, and the value of
 * <code>document.cacheDuration</code> is reset to 0. To enter streaming mode,
 * call <code>document.container.stream()</code> (see below for details). Note
 * that you must determine output characteristics (
 * <code>document.container.mediaType</code>,
 * <code>document.container.characterSet</code>, and
 * <code>document.container.language</code>) <i>before</i> entering streaming
 * mode. Trying to change them while running in streaming mode will raise an
 * exception.
 * </ul>
 * <p>
 * A special container environment is created for scriptlets, with some useful
 * services. It is available to scriptlets as a global variable named
 * <code>document.container</code>. For some other global variables available to
 * scriptlets, see {@link Document}.
 * <p>
 * Operations:
 * <ul>
 * <li><code>document.container.include(name)</code>: This powerful method
 * allows scriptlets to execute other documents in place, and is useful for
 * creating large, maintainable applications based on documents. Included
 * documents can act as a library or toolkit and can even be shared among many
 * applications. The included document does not have to be in the same
 * programming language or use the same engine as the calling scriptlet.
 * However, if they do use the same engine, then methods, functions, modules,
 * etc., could be shared.
 * <p>
 * It is important to note that how this works varies a lot per engine. For
 * example, in JRuby, every scriptlet is run in its own scope, so that sharing
 * would have to be done explicitly in the global scope. See the included JRuby
 * examples for a discussion of various ways to do this.
 * </li>
 * <li><code>document.container.include(name, engineName)</code>: As above,
 * except that the document is parsed as a single, non-delimited scriptlet. As
 * such, you must explicitly specify the name of the scripting engine that
 * should evaluate it.</li>
 * <li><code>document.container.stream()</code>: If you are in caching mode,
 * calling this method will return true and cause the document to run again,
 * where this next run will be in streaming mode. Whatever output the document
 * created in the current run is discarded, and all further exceptions are
 * ignored. For this reason, it's probably best to call
 * <code>document.container.stream()</code> as early as possible in the
 * document, and then to quit the document as soon as possible if it returns
 * true. For example, your document can start by testing whether it will have a
 * lot of output, and if so, set output characteristics, call
 * <code>document.container.stream()</code>, and quit. If you are already in
 * streaming mode, calling this method has no effect and returns false. Note
 * that a good way to quit the script is to throw an exception, because it will
 * end the script and otherwise be ignored. By default, writers will be
 * automatically flushed after every line in streaming mode. If you want to
 * disable this behavior, use <code>document.container.stream(flushLines)</code>
 * .</li>
 * <li><code>document.container.stream(flushLines)</code>: This version of the
 * above adds a boolean argument to let you control whether to flush the writer
 * after every line in streaming mode. By default auto-flushing is enabled.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>document.container.isStreaming</code>: This boolean is true when
 * the writer is in streaming mode (see above).</li>
 * <li><code>document.container.request</code>: The {@link Request}. Useful for
 * accessing URL attributes, form parameters, etc.</li>
 * <li><code>document.container.response</code>: The {@link Response}. Useful
 * for explicitly setting response characteristics.</li>
 * <li><code>document.container.source</code>: The source used for the script;
 * see {@link #getDocumentSource()}.</li>
 * <li><code>document.container.variant</code>: The {@link Variant} of this
 * request. Useful for interrogating the client's preferences.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>document.container.characterSet</code>: The {@link CharacterSet}
 * that will be used for the generated string. Defaults to what the client
 * requested (in <code>document.container.variant</code>), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it. If not in
 * streaming mode, your scriptlets can change this to something else.</li>
 * <li><code>document.container.language</code>: The {@link Language} that will
 * be used for the generated string. Defaults to null. If not in streaming mode,
 * your scriptlets can change this to something else.</li>
 * <li><code>document.container.mediaType</code>: The {@link MediaType} that
 * will be used for the generated string. Defaults to what the client requested
 * (in <code>document.container.variant</code>). If not in streaming mode, your
 * scriptlets can change this to something else.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link ScriptletController} can be set to add
 * your own global variables to scriptlets. See
 * {@link #getScriptletController()}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.allowCompilation:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowCompilation()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.cache:</code>
 * {@link ConcurrentMap}, defaults to a new instance of
 * {@link ConcurrentHashMap}. See {@link #getCache()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName:</code>
 * {@link String}, defaults to "index.page". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultScriptEngineName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b> See {@link #getDocumentSource()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.scriptletController:</code>
 * {@link ScriptletController}. See {@link #getScriptletController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getScriptEngineManager()}.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 * @see Document
 * @see DelegatedResource
 */
public class GeneratedTextResource extends ServerResource
{
	// Temporary hack due to Restlet bug!
	private volatile List<Variant> variants;

	@Override
	public List<Variant> getVariants()
	{
		List<Variant> v = this.variants;
		if( v == null )
		{
			synchronized( this )
			{
				v = this.variants;
				if( v == null )
				{
					this.variants = v = new ArrayList<Variant>();
				}
			}
		}
		return v;
	}

	//
	// Attributes
	//

	/**
	 * The {@link Writer} used by the {@link Document}.
	 * 
	 * @return The writer
	 * @see #setWriter(Writer)
	 */
	public Writer getWriter()
	{
		return this.writer;
	}

	/**
	 * @param writer
	 *        The writer
	 * @see #getWriter()
	 */
	public void setWriter( Writer writer )
	{
		this.writer = writer;
	}

	/**
	 * Same as {@link #getWriter()}, for standard error. (Nothing is currently
	 * done with the contents of this, but this may change in future
	 * implementations.)
	 * 
	 * @return The error writer
	 * @see #setErrorWriter(Writer)
	 */
	public Writer getErrorWriter()
	{
		return this.errorWriter;
	}

	/**
	 * @param errorWriter
	 *        The error writer
	 * @see #getErrorWriter()
	 */
	public void setErrorWriter( Writer errorWriter )
	{
		this.errorWriter = errorWriter;
	}

	/**
	 * Cache used for caching mode. Defaults to a new instance of
	 * {@link ConcurrentHashMap}. It is stored in the application's
	 * {@link Context} for persistence across requests and for sharing among
	 * instances of {@link GeneratedTextResource}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.cache</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The cache
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, RepresentableString> getCache()
	{
		if( this.cache == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.cache = (ConcurrentMap<String, RepresentableString>) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.cache" );
			if( this.cache == null )
			{
				this.cache = new ConcurrentHashMap<String, RepresentableString>();

				ConcurrentMap<String, RepresentableString> existing = (ConcurrentMap<String, RepresentableString>) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.cache", this.cache );
				if( existing != null )
					this.cache = existing;
			}
		}

		return this.cache;
	}

	/**
	 * The default character set to be used if the client does not specify it.
	 * Defaults to {@link CharacterSet#UTF_8}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public CharacterSet getDefaultCharacterSet()
	{
		if( this.defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet" );

			if( this.defaultCharacterSet == null )
				this.defaultCharacterSet = CharacterSet.UTF_8;
		}

		return this.defaultCharacterSet;
	}

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs that do not contain
	 * filenames. Defaults to "index.page".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( this.defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultName" );

			if( this.defaultName == null )
				this.defaultName = "index.page";
		}

		return this.defaultName;
	}

	/**
	 * The default script engine name to be used if the first scriptlet doesn't
	 * specify one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default script engine name
	 */
	public String getDefaultScriptEngineName()
	{
		if( this.defaultScriptEngineName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultScriptEngineName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName" );

			if( this.defaultScriptEngineName == null )
				this.defaultScriptEngineName = "js";
		}

		return this.defaultScriptEngineName;
	}

	/**
	 * An optional {@link ScriptletController} to be used with the scripts.
	 * Useful for adding your own global variables to the script.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.scriptletController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script context controller or null if none used
	 */
	public ScriptletController getScriptletController()
	{
		if( this.scriptletController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptletController = (ScriptletController) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.scriptletController" );
		}

		return this.scriptletController;
	}

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts. Uses a default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script engine manager
	 */
	public ScriptEngineManager getScriptEngineManager()
	{
		if( this.scriptEngineManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptEngineManager = (ScriptEngineManager) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager" );

			if( this.scriptEngineManager == null )
			{
				this.scriptEngineManager = new ScriptEngineManager();

				ScriptEngineManager existing = (ScriptEngineManager) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager", this.scriptEngineManager );
				if( existing != null )
					this.scriptEngineManager = existing;
			}
		}

		return this.scriptEngineManager;
	}

	/**
	 * The {@link DocumentSource} used to fetch scripts. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.documentSource</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	@SuppressWarnings("unchecked")
	public DocumentSource<Document> getDocumentSource()
	{
		if( this.documentSource == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.documentSource = (DocumentSource<Document>) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.documentSource" );

			if( this.documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.GeneratedTextResource.documentSource must be set in context to use ScriptResource" );
		}

		return this.documentSource;
	}

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.allowCompilation</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow compilation
	 */
	public boolean isAllowCompilation()
	{
		if( this.allowCompilation == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.allowCompilation = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.allowCompilation" );

			if( this.allowCompilation == null )
				this.allowCompilation = true;
		}

		return this.allowCompilation;
	}

	/**
	 * This is so we can see the source code for documents by adding
	 * <code>?source=true</code> to the URL. You probably wouldn't want this for
	 * most applications. Defaults to false.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.sourceViewable</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of document source code
	 */
	public boolean isSourceViewable()
	{
		if( this.sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.sourceViewable" );

			if( this.sourceViewable == null )
				this.sourceViewable = false;
		}

		return this.sourceViewable;
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
		List<Variant> variants = getVariants();
		variants.add( new Variant( MediaType.TEXT_HTML ) );
		variants.add( new Variant( MediaType.TEXT_PLAIN ) );
	}

	@Override
	public Representation get() throws ResourceException
	{
		// TODO: is this really what we want to do here?
		return get( null );
	}

	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		Request request = getRequest();
		String name = PrudenceUtils.getRelativePart( request, getDefaultName() );

		try
		{
			if( isSourceViewable() && TRUE.equals( request.getResourceRef().getQueryAsForm().getFirstValue( SOURCE ) ) )
			{
				// Represent document source
				return new StringRepresentation( getDocumentSource().getDocumentDescriptor( name ).getText() );
			}
			else
			{
				// Run document and represent its output
				ExposedContainerForGeneratedTextResource container = new ExposedContainerForGeneratedTextResource( this, variant, getCache() );
				Representation representation = container.include( name );

				if( representation == null )
					throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
				else
					return representation;
			}
		}
		catch( FileNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}
		catch( ScriptException x )
		{
			throw new ResourceException( x );
		}
	}

	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		// Handle the same was as get(variant)
		return get( entity );
	}

	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		// Handle the same was as post(entity) -- we are ignoring the variant
		return post( entity );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts.
	 */
	private ScriptEngineManager scriptEngineManager;

	/**
	 * The {@link DocumentSource} used to fetch scripts.
	 */
	private DocumentSource<Document> documentSource;

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private String defaultName;

	/**
	 * The default script engine name to be used if the script doesn't specify
	 * one.
	 */
	private String defaultScriptEngineName;

	/**
	 * The default character set to be used if the client does not specify it.
	 */
	private CharacterSet defaultCharacterSet;

	/**
	 * An optional {@link ScriptletController} to be used with the scripts.
	 */
	private ScriptletController scriptletController;

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it.
	 */
	private Boolean allowCompilation;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	private Boolean sourceViewable;

	/**
	 * Constant.
	 */
	private static final String SOURCE = "source";

	/**
	 * Constant.
	 */
	private static final String TRUE = "true";

	/**
	 * Cache used for caching mode.
	 */
	private ConcurrentMap<String, RepresentableString> cache;

	/**
	 * Same as {@link #writer}, for standard error. (Nothing is currently done
	 * with the contents of this, but this may change in future
	 * implementations.)
	 */
	private Writer errorWriter = new StringWriter();

	/**
	 * The {@link Writer} used by the {@link Document}.
	 */
	private Writer writer;
}