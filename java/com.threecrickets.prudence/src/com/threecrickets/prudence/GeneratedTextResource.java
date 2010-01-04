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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngineManager;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
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
import com.threecrickets.prudence.util.PygmentsSourceRepresenter;
import com.threecrickets.prudence.util.RepresentableString;
import com.threecrickets.prudence.util.SourceRepresenter;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.ScriptletController;
import com.threecrickets.scripturian.DocumentSource.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentInitializationException;
import com.threecrickets.scripturian.exception.DocumentRunException;

/**
 * A Restlet resource which runs a Scripturian {@link Document} for HTTP GET and
 * POST verbs and redirects its standard output to a
 * {@link StringRepresentation}.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getDocumentSource()}. This source
 * is accessible from scriptlets, via <code>prudence.source</code>.
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
 * changing the values of <code>prudence.mediaType</code>,
 * <code>prudence.characterSet</code>, and <code>prudence.language</code> (see
 * below).</li>
 * <li>Streaming mode: Output is sent to the client <i>while</i> scriptlets run.
 * This is recommended for documents that need to output a very large amount of
 * text, which might take a long time, or that might otherwise encounter
 * slow-downs while running. In either case, you want the client to receive
 * ongoing output. The output of the document is not cached, and the value of
 * <code>document.cacheDuration</code> is reset to 0. To enter streaming mode,
 * call <code>prudence.stream()</code> (see below for details). Note that you
 * must determine output characteristics ( <code>prudence.mediaType</code>,
 * <code>prudence.characterSet</code>, and <code>prudence.language</code>)
 * <i>before</i> entering streaming mode. Trying to change them while running in
 * streaming mode will raise an exception.
 * </ul>
 * <p>
 * A special container environment is created for scriptlets, with some useful
 * services. It is available to scriptlets as a global variable named
 * <code>prudence</code>. For some other global variables available to
 * scriptlets, see {@link Document}.
 * <p>
 * Operations:
 * <ul>
 * <li><code>prudence.includeDocument(name)</code>: This powerful method allows
 * scriptlets to execute other documents in place, and is useful for creating
 * large, maintainable applications based on documents. Included documents can
 * act as a library or toolkit and can even be shared among many applications.
 * The included document does not have to be in the same programming language or
 * use the same engine as the calling scriptlet. However, if they do use the
 * same engine, then methods, functions, modules, etc., could be shared.
 * <p>
 * It is important to note that how this works varies a lot per engine. For
 * example, in JRuby, every scriptlet is run in its own scope, so that sharing
 * would have to be done explicitly in the global scope. See the included JRuby
 * examples for a discussion of various ways to do this.
 * </li>
 * <li><code>prudence.include(name)</code>:except that the document is parsed as
 * a single, non-delimited script with the engine name derived from name's
 * extension.</li>
 * <li><code>prudence.stream()</code>: If you are in caching mode, calling this
 * method will return true and cause the document to run again, where this next
 * run will be in streaming mode. Whatever output the document created in the
 * current run is discarded, and all further exceptions are ignored. For this
 * reason, it's probably best to call <code>prudence.stream()</code> as early as
 * possible in the document, and then to quit the document as soon as possible
 * if it returns true. For example, your document can start by testing whether
 * it will have a lot of output, and if so, set output characteristics, call
 * <code>prudence.stream()</code>, and quit. If you are already in streaming
 * mode, calling this method has no effect and returns false. Note that a good
 * way to quit the script is to throw an exception, because it will end the
 * script and otherwise be ignored. By default, writers will be automatically
 * flushed after every line in streaming mode. If you want to disable this
 * behavior, use <code>prudence.stream(flushLines)</code> .</li>
 * <li><code>prudence.stream(flushLines)</code>: This version of the above adds
 * a boolean argument to let you control whether to flush the writer after every
 * line in streaming mode. By default line-by-line flushing is enabled.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>prudence.entity</code>: The entity of this request. Available only
 * for post and put.</li>
 * <li><code>prudence.isStreaming</code>: This boolean is true when the writer
 * is in streaming mode (see above).</li>
 * <li><code>prudence.resource</code>: The instance of this resource. Acts as a
 * "this" reference for scriptlets. You can use it to access the request and
 * response.</li>
 * <li><code>prudence.source</code>: The source used for the script; see
 * {@link #getDocumentSource()}.</li>
 * <li><code>prudence.variant</code>: The {@link Variant} of this request.
 * Useful for interrogating the client's preferences.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>prudence.characterSet</code>: The {@link CharacterSet} that will be
 * used for the generated string. Defaults to what the client requested (in
 * <code>prudence.variant</code>), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it. If not in
 * streaming mode, your scriptlets can change this to something else.</li>
 * <li><code>prudence.language</code>: The {@link Language} that will be used
 * for the generated string. Defaults to null. If not in streaming mode, your
 * scriptlets can change this to something else.</li>
 * <li><code>prudence.mediaType</code>: The {@link MediaType} that will be used
 * for the generated string. Defaults to what the client requested (in
 * <code>prudence.variant</code>). If not in streaming mode, your scriptlets can
 * change this to something else.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link ScriptletController} can be set to add
 * your own global variables to scriptlets. See
 * {@link #getScriptletController()}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.allowClientCaching:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowClientCaching()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.allowCompilation:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowCompilation()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.cache:</code>
 * {@link ConcurrentMap}, defaults to a new instance of
 * {@link ConcurrentHashMap}. See {@link #getCache()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.containerName</code>:
 * The name of the global variable with which to access the container. Defaults
 * to "prudence". See {@link #getContainerName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName:</code>
 * {@link String}, defaults to "index.page". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultEngineName()}.</li>
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
 * <code>com.threecrickets.prudence.GeneratedTextResource.engineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getEngineManager()}.</li>
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
		return writer;
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
		return errorWriter;
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
	 * The name of the global variable with which to access the container.
	 * Defaults to "prudence".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.containerName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The container name
	 */
	public String getContainerName()
	{
		if( containerName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			containerName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.containerName" );

			if( containerName == null )
				containerName = "prudence";
		}

		return containerName;
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
		if( cache == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			cache = (ConcurrentMap<String, RepresentableString>) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.cache" );
			if( cache == null )
			{
				cache = new ConcurrentHashMap<String, RepresentableString>();

				ConcurrentMap<String, RepresentableString> existing = (ConcurrentMap<String, RepresentableString>) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.cache", cache );
				if( existing != null )
					cache = existing;
			}
		}

		return cache;
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
		if( defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

	/**
	 * If a URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs that do not contain
	 * filenames. Defaults to "index".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultName" );

			if( defaultName == null )
				defaultName = "index";
		}

		return defaultName;
	}

	/**
	 * The default script engine name to be used if the first scriptlet doesn't
	 * specify one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.defaultEngineName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default script engine name
	 */
	public String getDefaultEngineName()
	{
		if( defaultEngineName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultEngineName = (String) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.defaultEngineName" );

			if( defaultEngineName == null )
				defaultEngineName = "js";
		}

		return defaultEngineName;
	}

	/**
	 * An optional {@link ScriptletController} to be used with the scriptlets.
	 * Useful for adding your own global variables to the scriptlets.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.scriptletController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script context controller or null if none used
	 */
	public ScriptletController getScriptletController()
	{
		if( scriptletController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			scriptletController = (ScriptletController) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.scriptletController" );
		}

		return scriptletController;
	}

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts. Uses a default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.engineManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script engine manager
	 */
	public ScriptEngineManager getEngineManager()
	{
		if( scriptEngineManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			scriptEngineManager = (ScriptEngineManager) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.engineManager" );

			if( scriptEngineManager == null )
			{
				scriptEngineManager = new ScriptEngineManager();

				ScriptEngineManager existing = (ScriptEngineManager) attributes.putIfAbsent( "com.threecrickets.prudence.GeneratedTextResource.engineManager", scriptEngineManager );
				if( existing != null )
					scriptEngineManager = existing;
			}
		}

		return scriptEngineManager;
	}

	/**
	 * The {@link DocumentSource} used to fetch documents. This must be set to a
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
		if( documentSource == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentSource = (DocumentSource<Document>) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.GeneratedTextResource.documentSource must be set in context to use GeneratedTextResource" );
		}

		return documentSource;
	}

	/**
	 * Whether or not to send information to the client about cache expiration.
	 * Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.GeneratedTextResource.allowClientCaching</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isAllowClientCaching()
	{
		if( allowClientCaching == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			allowClientCaching = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.allowClientCaching" );

			if( allowClientCaching == null )
				allowClientCaching = true;
		}

		return allowClientCaching;
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
		if( allowCompilation == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			allowCompilation = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.allowCompilation" );

			if( allowCompilation == null )
				allowCompilation = true;
		}

		return allowCompilation;
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
		if( sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.GeneratedTextResource.sourceViewable" );

			if( sourceViewable == null )
				sourceViewable = false;
		}

		return sourceViewable;
	}

	/**
	 * An optional {@link SourceRepresenter} to use for representing source
	 * code.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.sourceRepresenter</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The source representer or null
	 * @see #isSourceViewable()
	 */
	public SourceRepresenter getSourceRepresenter()
	{
		if( sourceRepresenter == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			sourceRepresenter = (SourceRepresenter) attributes.get( "com.threecrickets.prudence.DelegatedResource.sourceRepresenter" );

			if( sourceRepresenter == null )
			{
				// sourceRepresenter = new SyntaxHighlighterSourceRepresenter(
				// getContext() );
				sourceRepresenter = new PygmentsSourceRepresenter();

				SourceRepresenter existing = (SourceRepresenter) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.sourceRepresenter", sourceRepresenter );
				if( existing != null )
					sourceRepresenter = existing;
			}
		}

		return sourceRepresenter;
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
	}

	@Override
	public Representation get() throws ResourceException
	{
		return run( null, null );
	}

	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		return run( null, variant );
	}

	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		return run( entity, null );
	}

	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		return run( entity, variant );
	}

	@Override
	public Representation put( Representation entity ) throws ResourceException
	{
		return run( entity, null );
	}

	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		return run( entity, variant );
	}

	@Override
	public Representation delete() throws ResourceException
	{
		return run( null, null );
	}

	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		return run( null, variant );
	}

	@Override
	public Representation options() throws ResourceException
	{
		return run( null, null );
	}

	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		return run( null, variant );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts.
	 */
	private volatile ScriptEngineManager scriptEngineManager;

	/**
	 * The {@link DocumentSource} used to fetch scripts.
	 */
	private volatile DocumentSource<Document> documentSource;

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private volatile String defaultName;

	/**
	 * The default script engine name to be used if the script doesn't specify
	 * one.
	 */
	private volatile String defaultEngineName;

	/**
	 * The default character set to be used if the client does not specify it.
	 */
	private volatile CharacterSet defaultCharacterSet;

	/**
	 * An optional {@link ScriptletController} to be used with the scripts.
	 */
	private volatile ScriptletController scriptletController;

	/**
	 * Whether or not to send information to the client about cache expiration.
	 */
	private volatile Boolean allowClientCaching;

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it.
	 */
	private volatile Boolean allowCompilation;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	private volatile Boolean sourceViewable;

	/**
	 * Cache used for caching mode.
	 */
	private volatile ConcurrentMap<String, RepresentableString> cache;

	/**
	 * Same as {@link #writer}, for standard error. (Nothing is currently done
	 * with the contents of this, but this may change in future
	 * implementations.)
	 */
	private volatile Writer errorWriter = new StringWriter();

	/**
	 * The {@link Writer} used by the {@link Document}.
	 */
	private volatile Writer writer;

	/**
	 * The name of the global variable with which to access the container.
	 */
	private volatile String containerName;

	/**
	 * The source code formatter.
	 */
	private volatile SourceRepresenter sourceRepresenter;

	/**
	 * Constant.
	 */
	private static final String SOURCE = "source";

	/**
	 * Constant.
	 */
	private static final String LINE = "line";

	/**
	 * Constant.
	 */
	private static final String TRUE = "true";

	/**
	 * Does the actual handling of requests.
	 * 
	 * @param entity
	 *        The entity
	 * @param variant
	 *        The variant
	 * @return A representation
	 * @throws ResourceException
	 */
	private Representation run( Representation entity, Variant variant ) throws ResourceException
	{
		Request request = getRequest();
		String name = PrudenceUtils.getRemainingPart( request, getDefaultName() );

		try
		{
			if( isSourceViewable() )
			{
				Form query = request.getResourceRef().getQueryAsForm();
				if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
				{
					int lineNumber = -1;
					String line = query.getFirstValue( LINE );
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
					DocumentDescriptor<Document> documentDescriptor = getDocumentSource().getDocumentDescriptor( name );
					SourceRepresenter sourceRepresenter = getSourceRepresenter();
					if( sourceRepresenter != null )
						return sourceRepresenter.representSource( name, lineNumber, documentDescriptor, request );
					else
						return new StringRepresentation( documentDescriptor.getText() );
				}
			}

			// Run document and represent its output
			ExposedContainerForGeneratedTextResource container = new ExposedContainerForGeneratedTextResource( this, entity, variant, getCache() );
			Representation representation = container.includeDocument( name );

			if( representation == null )
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
			else
				return representation;
		}
		catch( FileNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}
		catch( DocumentInitializationException x )
		{
			throw new ResourceException( x );
		}
		catch( DocumentRunException x )
		{
			throw new ResourceException( x );
		}
	}
}