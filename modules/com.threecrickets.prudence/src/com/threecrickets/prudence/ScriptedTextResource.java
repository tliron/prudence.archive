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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.internal.ExposedScriptedTextResourceContainer;
import com.threecrickets.prudence.internal.RepresentableString;
import com.threecrickets.prudence.internal.PrudenceUtils;
import com.threecrickets.scripturian.CompositeScript;
import com.threecrickets.scripturian.ScriptContextController;
import com.threecrickets.scripturian.ScriptSource;

/**
 * A Restlet resource which runs a {@link CompositeScript} and redirects its
 * standard output to a {@link Representation}, for both HTTP GET and POST
 * verbs.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getScriptSource()}. This source is
 * accessible from the script itself, via <code>container.script.source</code>.
 * <p>
 * This resource supports two modes of output:
 * <ul>
 * <li>Caching mode: First, the entire script is run, with its output sent into
 * a buffer. This buffer is then cached, and <i>only then</i> sent to the
 * client. This is the default mode and recommended for most scripts. Scripts
 * can control the duration of their individual cache by changing the value of
 * <code>script.cacheDuration</code> (see {@link CompositeScript}). Because
 * output is not sent to the client until after the script finished its run, it
 * is possible for the script to determine output characteristics at any time by
 * changing the values of <code>container.mediaType</code>,
 * <code>script.container.characterSet</code>, and
 * <code>script.container.language</code> (see below).</li>
 * <li>Streaming mode: Output is sent to the client <i>while</i> the script
 * runs. This is recommended for scripts that need to output a very large amount
 * of string, which might take a long time, or that might otherwise encounter
 * slow-downs while running. In either case, you want the client to receive
 * ongoing output. The output of the script is not cached, and the value of
 * <code>script.cacheDuration</code> is reset to 0. To enter streaming mode,
 * call <code>script.container.stream()</code> (see below for details). Note
 * that you must determine output characteristics (
 * <code>script.container.mediaType</code>,
 * <code>script.container.characterSet</code>, and
 * <code>script.container.language</code>) <i>before</i> entering streaming
 * mode. Trying to change them while running in streaming mode will raise an
 * exception.
 * </ul>
 * <p>
 * A special container environment is created for scripts, with some useful
 * services. It is available to the script as a global variable named
 * <code>script.container</code>. For some other global variables available to
 * scripts, see {@link CompositeScript}.
 * <p>
 * Operations:
 * <ul>
 * <li><code>script.container.include(name)</code>: This powerful method allows
 * scripts to execute other scripts in place, and is useful for creating large,
 * maintainable applications based on scripts. Included scripts can act as a
 * library or toolkit and can even be shared among many applications. The
 * included script does not have to be in the same language or use the same
 * engine as the calling script. However, if they do use the same engine, then
 * methods, functions, modules, etc., could be shared. It is important to note
 * that how this works varies a lot per scripting platform. For example, in
 * JRuby, every script is run in its own scope, so that sharing would have to be
 * done explicitly in the global scope. See the included Ruby composite script
 * example for a discussion of various ways to do this.</li>
 * <li><code>script.container.include(name, engineName)</code>: As the above,
 * except that the script is not composite. As such, you must explicitly specify
 * the name of the scripting engine that should evaluate it.</li>
 * <li><code>script.container.stream()</code>: If you are in caching mode,
 * calling this method will return true and cause the script to run again, where
 * this next run will be in streaming mode. Whatever output the script created
 * in the current run is discarded, and all further exceptions are ignored. For
 * this reason, it's probably best to call
 * <code>script.container.stream()</code> as early as possible in the script,
 * and then to quit the script as soon as possible if it returns true. For
 * example, your script can start by testing whether it will have a lot of
 * output, and if so, set output characteristics, call
 * <code>script.container.stream()</code>, and quit. If you are already in
 * streaming mode, calling this method has no effect and returns false. Note
 * that a good way to quit the script is to throw an exception, because it will
 * end the script and otherwise be ignored. By default, writers will be
 * automatically flushed after every line in streaming mode. If you want to
 * disable this behavior, use <code>script.container.stream(flushLines)</code>.</li>
 * <li><code>script.container.stream(flushLines)</code>: This version of the
 * above adds a boolean argument to let you control whether to flush the writer
 * after every line in streaming mode. By default auto-flushing is enabled.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>script.container.isStreaming</code>: This boolean is true when the
 * writer is in streaming mode (see above).</li>
 * <li><code>script.container.request</code>: The {@link Request}. Useful for
 * accessing URL attributes, form parameters, etc.</li>
 * <li><code>script.container.response</code>: The {@link Response}. Useful for
 * explicitly setting response characteristics.</li>
 * <li><code>script.container.source</code>: The source used for the script; see
 * {@link #getScriptSource()}.</li>
 * <li><code>script.container.variant</code>: The {@link Variant} of this
 * request. Useful for interrogating the client's preferences.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>script.container.characterSet</code>: The {@link CharacterSet} that
 * will be used for the generated string. Defaults to what the client requested
 * (in <code>script.container.variant</code>), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it. If not in
 * streaming mode, your script can change this to something else.</li>
 * <li><code>script.container.language</code>: The {@link Language} that will be
 * used for the generated string. Defaults to null. If not in streaming mode,
 * your script can change this to something else.</li>
 * <li><code>script.container.mediaType</code>: The {@link MediaType} that will
 * be used for the generated string. Defaults to what the client requested (in
 * <code>script.container.variant</code>). If not in streaming mode, your script
 * can change this to something else.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link ScriptContextController} can be set to add
 * your own global variables to each composite script. See
 * {@link #getScriptContextController()}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.allowCompilation:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowCompilation()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.cache:</code>
 * {@link ConcurrentMap}, defaults to a new instance of
 * {@link ConcurrentHashMap}. See {@link #getCache()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.defaultName:</code>
 * {@link String}, defaults to "index.page". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.defaultScriptEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultScriptEngineName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.scriptContextController:</code>
 * {@link ScriptContextController}. See {@link #getScriptContextController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.scriptEngineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getScriptEngineManager()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ScriptedTextResource.scriptSource:</code>
 * {@link ScriptSource}. <b>Required.</b> See {@link #getScriptSource()}.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 * @see CompositeScript
 * @see ScriptedResource
 */
public class ScriptedTextResource extends ServerResource
{
	//
	// Attributes
	//

	/**
	 * The {@link Writer} used by the {@link CompositeScript}.
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
	 * instances of {@link ScriptedTextResource}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.cache</code> in
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
			this.cache = (ConcurrentMap<String, RepresentableString>) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.cache" );
			if( this.cache == null )
			{
				this.cache = new ConcurrentHashMap<String, RepresentableString>();

				ConcurrentMap<String, RepresentableString> existing = (ConcurrentMap<String, RepresentableString>) attributes.putIfAbsent( "com.threecrickets.prudence.ScriptedTextResource.cache", this.cache );
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
	 * <code>com.threecrickets.prudence.ScriptedTextResource.defaultCharacterSet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public CharacterSet getDefaultCharacterSet()
	{
		if( this.defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.defaultCharacterSet" );

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
	 * <code>com.threecrickets.prudence.ScriptedTextResource.defaultName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( this.defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultName = (String) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.defaultName" );

			if( this.defaultName == null )
				this.defaultName = "index.page";
		}

		return this.defaultName;
	}

	/**
	 * The default script engine name to be used if the script doesn't specify
	 * one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.defaultScriptEngineName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default script engine name
	 */
	public String getDefaultScriptEngineName()
	{
		if( this.defaultScriptEngineName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultScriptEngineName = (String) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.defaultScriptEngineName" );

			if( this.defaultScriptEngineName == null )
				this.defaultScriptEngineName = "js";
		}

		return this.defaultScriptEngineName;
	}

	/**
	 * An optional {@link ScriptContextController} to be used with the scripts.
	 * Useful for adding your own global variables to the script.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.scriptContextController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script context controller or null if none used
	 */
	public ScriptContextController getScriptContextController()
	{
		if( this.scriptContextController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptContextController = (ScriptContextController) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.scriptContextController" );
		}

		return this.scriptContextController;
	}

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts. Uses a default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.scriptEngineManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script engine manager
	 */
	public ScriptEngineManager getScriptEngineManager()
	{
		if( this.scriptEngineManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptEngineManager = (ScriptEngineManager) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.scriptEngineManager" );

			if( this.scriptEngineManager == null )
			{
				this.scriptEngineManager = new ScriptEngineManager();

				ScriptEngineManager existing = (ScriptEngineManager) attributes.putIfAbsent( "com.threecrickets.prudence.ScriptedTextResource.scriptEngineManager", this.scriptEngineManager );
				if( existing != null )
					this.scriptEngineManager = existing;
			}
		}

		return this.scriptEngineManager;
	}

	/**
	 * The {@link ScriptSource} used to fetch scripts. This must be set to a
	 * valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.scriptSource</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script source
	 */
	@SuppressWarnings("unchecked")
	public ScriptSource<CompositeScript> getScriptSource()
	{
		if( this.scriptSource == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptSource = (ScriptSource<CompositeScript>) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.scriptSource" );

			if( this.scriptSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.ScriptedTextResource.scriptSource must be set in context to use ScriptResource" );
		}

		return this.scriptSource;
	}

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.allowCompilation</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow compilation
	 */
	public boolean isAllowCompilation()
	{
		if( this.allowCompilation == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.allowCompilation = (Boolean) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.allowCompilation" );

			if( this.allowCompilation == null )
				this.allowCompilation = true;
		}

		return this.allowCompilation;
	}

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL. You probably wouldn't want this for
	 * most applications. Defaults to false.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.ScriptedTextResource.sourceViewable</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of script source code
	 */
	public boolean isSourceViewable()
	{
		if( this.sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.ScriptedTextResource.sourceViewable" );

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
		List<MediaType> mediaTypes = Arrays.asList( new MediaType[]
		{
			MediaType.TEXT_HTML, MediaType.TEXT_PLAIN
		} );
		Map<Method, Object> variants = getVariants();
		variants.put( Method.GET, mediaTypes );
		variants.put( Method.POST, mediaTypes );
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
				// Represent script source
				return new StringRepresentation( getScriptSource().getScriptDescriptor( name ).getText() );
			}
			else
			{
				// Run script and represent its output
				ExposedScriptedTextResourceContainer container = new ExposedScriptedTextResourceContainer( this, variant, getCache() );
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
	 * The {@link ScriptSource} used to fetch scripts.
	 */
	private ScriptSource<CompositeScript> scriptSource;

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
	 * An optional {@link ScriptContextController} to be used with the scripts.
	 */
	private ScriptContextController scriptContextController;

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
	 * The {@link Writer} used by the {@link CompositeScript}.
	 */
	private Writer writer;
}