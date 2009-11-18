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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptEngineManager;

import org.restlet.Context;
import org.restlet.Request;
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

import com.threecrickets.prudence.internal.ExposedContainerForDelegatedResource;
import com.threecrickets.prudence.internal.PrudenceUtils;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.ScriptletController;

/**
 * A Restlet resource which delegates functionality to a Scripturian
 * {@link Document} with well-defined entry points. The entry points must be
 * global functions, closures, or whatever other technique the scripting engine
 * uses to make entry points available to Java. They entry points are:
 * <ul>
 * <li><code>handleInit()</code>: This function is called when the resource is
 * initialized. We will use it set general characteristics for the resource.</li>
 * <li><code>handleGet()</code>: This function is called for the GET verb, which
 * is expected to behave as a logical "read" of the resource's state. The
 * expectation is that it return one representation, out of possibly many, of
 * the resource's state. Returned values can be of any explicit sub-class of
 * {@link Representation}. Other types will be automatically converted to string
 * representation using the client's requested media type and character set.
 * These, and the language of the representation (defaulting to null), can be
 * read and changed via <code>container.mediaType</code>,
 * <code>container.characterSet</code>, and <code>container.language</code>.
 * Additionally, you can use <code>container.variant</code> to interrogate the
 * client's provided list of supported languages and encoding.</li>
 * <li><code>handlePost()</code>: This function is called for the POST verb,
 * which is expected to behave as a logical "update" of the resource's state.
 * The expectation is that <code>container.entity</code> represents an update to
 * the state, that will affect future calls to <code>handleGet()</code>. As
 * such, it may be possible to accept logically partial representations of the
 * state. You may optionally return a representation, in the same way as
 * <code>handleGet()</code>. Because many scripting languages functions return
 * the last statement's value by default, you must explicitly return a null if
 * you do not want to return a representation to the client.</li>
 * <li><code>handlePut()</code>: This function is called for the PUT verb, which
 * is expected to behave as a logical "create" of the resource's state. The
 * expectation is that container.entity represents an entirely new state, that
 * will affect future calls to <code>handleGet()</code>. Unlike
 * <code>handlePost()</code>, it is expected that the representation be
 * logically complete. You may optionally return a representation, in the same
 * way as <code>handleGet()</code>. Because JavaScript functions return the last
 * statement's value by default, you must explicitly return a null if you do not
 * want to return a representation to the client.</li>
 * <li><code>handleDelete()</code>: This function is called for the DELETE verb,
 * which is expected to behave as a logical "delete" of the resource's state.
 * The expectation is that subsequent calls to <code>handleGet()</code> will
 * fail. As such, it doesn't make sense to return a representation, and any
 * returned value will ignored. Still, it's a good idea to return null to avoid
 * any passing of value.</li>
 * </ul>
 * <p>
 * Names of these entry point can be configured via attributes in the
 * application's {@link Context}. See {@link #getEntryPointNameForInit()},
 * {@link #getEntryPointNameForGet()}, {@link #getEntryPointNameForPost()},
 * {@link #getEntryPointNameForPut()} and {@link #getEntryPointNameForDelete()}.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getDocumentSource()}. This source
 * is accessible from the script itself, via
 * <code>document.container.source</code>.
 * <p>
 * Note that the composite script's output is sent to the system's standard
 * output. Most likely, you will not want to output anything from the script.
 * However, this redirection is provided as a convenience, which may be useful
 * for certain debugging situations.
 * <p>
 * A special container environment is created for scriptlets, with some useful
 * services. It is available to scriptlets as a global variable named
 * <code>document.container</code>. For some other global variables available to
 * scriptlets, see {@link Document}.
 * <p>
 * Operations:
 * <ul>
 * <li><code>document.container.includeDocument(name)</code>: This powerful
 * method allows scriptlets to execute other documents in place, and is useful
 * for creating large, maintainable applications based on documents. Included
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
 * <li><code>document.container.include(name)</code>:except that the document is
 * parsed as a single, non-delimited script with the engine name derived from
 * name's extension.</li>
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>document.container.entity</code>: The {@link Representation} of an
 * entity provided with this request. Available only in
 * <code>handlePost()</code> and <code>handlePut()</code>. Note that
 * <code>document.container.variant</code> is identical to
 * <code>document.container.entity</code> when available.</li>
 * <li><code>document.container.resource</code>: The instance of this resource.
 * Acts as a "this" reference for scriptlets. For example, during a call to
 * <code>handleInit()</code>, this can be used to change the characteristics of
 * the resource. Otherwise, you can use it to access the request and response.</li>
 * <li><code>document.container.source</code>: The source used for the document;
 * see {@link #getDocumentSource()}.</li>
 * <li><code>document.container.variant</code>: The {@link Variant} of this
 * request. Useful for interrogating the client's preferences. This is available
 * only in <code>handleGet()</code>, <code>handlePost()</code> and
 * <code>handlePut()</code>.</li>
 * <li><code>document.container.variants</code>: A map of possible variants or
 * media types supported by this resource. You should initialize this during a
 * call to <code>handleInit()</code>. Values for the map can be
 * {@link MediaType} constants, explicit {@link Variant} instances (in which
 * case these variants will be returned immediately for their media type without
 * calling the entry point), or a {@link List} containing both media types and
 * variants. Use map key {@link Method#ALL} to indicate support for all methods.
 * </li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>document.container.mediaType</code>: The {@link MediaType} that
 * will be used if you return an arbitrary type for <code>handleGet()</code>,
 * <code>handlePost()</code> and <code>handlePut()</code>. Defaults to what the
 * client requested (in <code>document.container.variant</code>).</li>
 * <li><code>document.container.characterSet</code>: The {@link CharacterSet}
 * that will be used if you return an arbitrary type for
 * <code>handleGet()</code>, <code>handlePost()</code> and
 * <code>handlePut()</code>. Defaults to what the client requested (in
 * <code>document.container.variant</code>), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it.</li>
 * <li><code>document.container.language</code>: The {@link Language} that will
 * be used if you return an arbitrary type for <code>handleGet()</code>,
 * <code>handlePost()</code> and <code>handlePut()</code>. Defaults to null.</li>
 * </ul>
 * <p>
 * In addition to the above, a {@link ScriptletController} can be set to add
 * your own global variables to each composite script. See
 * {@link #getScriptletController()}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.allowCompilation:</code>
 * {@link Boolean}, defaults to true. See {@link #isAllowCompilation()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.defaultName:</code>
 * {@link String}, defaults to "default.script". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultScriptEngineName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b> See {@link #getDocumentSource()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete:</code>
 * {@link String}, defaults to "handleDelete". See
 * {@link #getEntryPointNameForDelete()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGet:</code>
 * {@link String}, defaults to "handleGet". See
 * {@link #getEntryPointNameForGet()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit:</code>
 * {@link String}, defaults to "handleInit". See
 * {@link #getEntryPointNameForInit()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPost:</code>
 * {@link String}, defaults to "handlePost". See
 * {@link #getEntryPointNameForPost()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPut:</code>
 * {@link String}, defaults to "handlePut". See
 * {@link #getEntryPointNameForPut()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.errorWriter:</code>
 * {@link Writer}, defaults to standard error. See {@link #getErrorWriter()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.extension:</code>
 * {@link String}, defaults to "script". See {@link #getExtension()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.scriptletController:</code>
 * {@link ScriptletController}. See {@link #getScriptletController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.scriptEngineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getScriptEngineManager()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false. See {@link #isSourceViewable()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.writer:</code>
 * {@link Writer}, defaults to standard output. See {@link #getWriter()}.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 * @see Document
 * @see GeneratedTextResource
 */
public class DelegatedResource extends ServerResource
{
	//
	// Attributes
	//

	/**
	 * The {@link Writer} used by the {@link Document}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.writer</code> in the
	 * application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public Writer getWriter()
	{
		if( this.writer == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.writer = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedResource.writer" );

			if( this.writer == null )
				this.writer = new OutputStreamWriter( System.out );
		}

		return this.writer;
	}

	/**
	 * Same as {@link #getWriter()}, for standard error. Defaults to standard
	 * error.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.errorWriter</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public Writer getErrorWriter()
	{
		if( this.errorWriter == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.errorWriter = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedResource.errorWriter" );

			if( this.errorWriter == null )
				this.errorWriter = new OutputStreamWriter( System.out );
		}

		return this.errorWriter;
	}

	/**
	 * The default character set to be used if the client does not specify it.
	 * Defaults to {@link CharacterSet#UTF_8}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public CharacterSet getDefaultCharacterSet()
	{
		if( this.defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultCharacterSet" );

			if( this.defaultCharacterSet == null )
				this.defaultCharacterSet = CharacterSet.UTF_8;
		}

		return this.defaultCharacterSet;
	}

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "default.script".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultName</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( this.defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultName" );

			if( this.defaultName == null )
				this.defaultName = "default.script";
		}

		return this.defaultName;
	}

	/**
	 * The default script engine name to be used if the script doesn't specify
	 * one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default script engine name
	 */
	public String getDefaultScriptEngineName()
	{
		if( this.defaultScriptEngineName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.defaultScriptEngineName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName" );

			if( this.defaultScriptEngineName == null )
				this.defaultScriptEngineName = "js";
		}

		return this.defaultScriptEngineName;
	}

	/**
	 * The name of the <code>handleDelete()</code> entry point in the script.
	 * Defaults to "handleDelete".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleDelete()</code> entry point
	 */
	public String getEntryPointNameForDelete()
	{
		if( this.entryPointNameForDelete == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.entryPointNameForDelete = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete" );

			if( this.entryPointNameForDelete == null )
				this.entryPointNameForDelete = "handleDelete";
		}

		return this.entryPointNameForDelete;
	}

	/**
	 * The name of the <code>handleGet()</code> entry point in the script.
	 * Defaults to "handleGet".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGet</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleGet()</code> entry point
	 */
	public String getEntryPointNameForGet()
	{
		if( this.entryPointNameForGet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.entryPointNameForGet = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForGet" );

			if( this.entryPointNameForGet == null )
				this.entryPointNameForGet = "handleGet";
		}

		return this.entryPointNameForGet;
	}

	/**
	 * The name of the <code>handleInit()</code> entry point in the script.
	 * Defaults to "handleInit".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleInit()</code> entry point
	 */
	public String getEntryPointNameForInit()
	{
		if( this.entryPointNameForInit == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.entryPointNameForInit = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForInit" );

			if( this.entryPointNameForInit == null )
				this.entryPointNameForInit = "handleInit";
		}

		return this.entryPointNameForInit;
	}

	/**
	 * The name of the <code>handlePost()</code> entry point in the script.
	 * Defaults to "handlePost".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPost</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handlePost()</code> entry point
	 */
	public String getEntryPointNameForPost()
	{
		if( this.entryPointNameForPost == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.entryPointNameForPost = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForPost" );

			if( this.entryPointNameForPost == null )
				this.entryPointNameForPost = "handlePost";
		}

		return this.entryPointNameForPost;
	}

	/**
	 * The name of the <code>handlePut()</code> entry point in the script.
	 * Defaults to "handlePut".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPut</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handlePut()</code> entry point
	 */
	public String getEntryPointNameForPut()
	{
		if( this.entryPointNameForPut == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.entryPointNameForPut = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForPut" );

			if( this.entryPointNameForPut == null )
				this.entryPointNameForPut = "handlePut";
		}

		return this.entryPointNameForPut;
	}

	/**
	 * Files with this extension can have the extension omitted from the URL,
	 * allowing for nicer URLs. Defaults to "script".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.extension</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The extension
	 */
	public String getExtension()
	{
		if( this.extension == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.extension = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.extension" );

			if( this.extension == null )
				this.extension = "script";
		}

		return this.extension;
	}

	/**
	 * An optional {@link ScriptletController} to be used with the scripts.
	 * Useful for adding your own global variables to the script.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.scriptletController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script context controller or null if none used
	 */
	public ScriptletController getScriptletController()
	{
		if( this.scriptletController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptletController = (ScriptletController) attributes.get( "com.threecrickets.prudence.DelegatedResource.scriptletController" );
		}

		return this.scriptletController;
	}

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts. Uses a default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.scriptEngineManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script engine manager
	 */
	public ScriptEngineManager getScriptEngineManager()
	{
		if( this.scriptEngineManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.scriptEngineManager = (ScriptEngineManager) attributes.get( "com.threecrickets.prudence.DelegatedResource.scriptEngineManager" );

			if( this.scriptEngineManager == null )
			{
				this.scriptEngineManager = new ScriptEngineManager();

				ScriptEngineManager existing = (ScriptEngineManager) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.scriptEngineManager", this.scriptEngineManager );
				if( existing != null )
					this.scriptEngineManager = existing;
			}
		}

		return this.scriptEngineManager;
	}

	/**
	 * The {@link DocumentSource} used to fetch and cache documents. This must
	 * be set to a valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.documentSource</code>
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
			this.documentSource = (DocumentSource<Document>) attributes.get( "com.threecrickets.prudence.DelegatedResource.documentSource" );

			if( this.documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.DelegatedResource.documentSource must be set in context to use ScriptResource" );
		}

		return this.documentSource;
	}

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.allowCompilation</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow compilation
	 */
	public boolean isAllowCompilation()
	{
		if( this.allowCompilation == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.allowCompilation = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.allowCompilation" );

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
	 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of document source code
	 */
	public boolean isSourceViewable()
	{
		if( this.sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			this.sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.sourceViewable" );

			if( this.sourceViewable == null )
				this.sourceViewable = false;
		}

		return this.sourceViewable;
	}

	//
	// ServerResource
	//

	/**
	 * Initializes the resource, and delegates to the <code>handleInit()</code>
	 * entry point in the script.
	 * 
	 * @see #getEntryPointNameForInit()
	 */
	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();
		setAnnotated( false );
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants() );

		try
		{
			container.invoke( getEntryPointNameForInit() );
		}
		catch( ResourceException x )
		{
			x.printStackTrace();
		}
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the script.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForGet()
	 */
	@Override
	public Representation get() throws ResourceException
	{
		// TODO: is this really what we want to do here?
		return get( null );
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the script.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForGet()
	 */
	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants(), variant );

		Request request = getRequest();
		if( isSourceViewable() && TRUE.equals( request.getResourceRef().getQueryAsForm().getFirstValue( SOURCE ) ) )
		{
			// Represent document source
			String name = PrudenceUtils.getRelativePart( request, getDefaultName() );
			try
			{
				return new StringRepresentation( getDocumentSource().getDocumentDescriptor( name ).getText() );
			}
			catch( IOException x )
			{
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
			}
		}
		else
		{
			Object r = container.invoke( getEntryPointNameForGet() );

			if( r == null )
				return null;

			if( r instanceof Representation )
				return (Representation) r;
			else
				return new StringRepresentation( r.toString(), container.getMediaType(), container.getLanguage(), container.getCharacterSet() );
		}
	}

	/**
	 * Delegates to the <code>handlePost()</code> entry point in the script.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPost()
	 */
	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		// TODO: is this really what we want to do here?
		return post( entity, null );
	}

	/**
	 * Delegates to the <code>handlePost()</code> entry point in the script.
	 * 
	 * @param entity
	 *        The posted entity
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPost()
	 */
	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants(), entity, variant );

		Object r = container.invoke( getEntryPointNameForPost() );
		if( r != null )
		{
			if( r instanceof Representation )
				return (Representation) r;
			else
				return new StringRepresentation( r.toString(), container.getMediaType(), container.getLanguage(), container.getCharacterSet() );
		}

		return null;
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the script.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPut()
	 */
	@Override
	public Representation put( Representation entity ) throws ResourceException
	{
		// TODO: is this really what we want to do here?
		return put( entity, null );
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the script.
	 * 
	 * @param entity
	 *        The posted entity
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForPut()
	 */
	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants(), entity, variant );

		Object r = container.invoke( getEntryPointNameForPut() );
		if( r != null )
		{
			if( r instanceof Representation )
				return (Representation) r;
			else
				return new StringRepresentation( r.toString(), container.getMediaType(), container.getLanguage(), container.getCharacterSet() );
		}

		return null;
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the script.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForDelete()
	 */
	@Override
	public Representation delete() throws ResourceException
	{
		// TODO: is this really what we want to do here?
		return delete( null );
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the script.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForDelete()
	 */
	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants(), variant );

		container.invoke( getEntryPointNameForDelete() );

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts.
	 */
	private ScriptEngineManager scriptEngineManager;

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it.
	 */
	private Boolean allowCompilation;

	/**
	 * The {@link DocumentSource} used to fetch scripts.
	 */
	private DocumentSource<Document> documentSource;

	/**
	 * Files with this extension can have the extension omitted from the URL,
	 * allowing for nicer URLs.
	 */
	private String extension;

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
	 * The name of the <code>handleInit()</code> entry point in the script.
	 */
	private String entryPointNameForInit;

	/**
	 * The name of the <code>handleGet()</code> entry point in the script.
	 */
	private String entryPointNameForGet;

	/**
	 * The name of the <code>handlePost()</code> entry point in the script.
	 */
	private String entryPointNameForPost;

	/**
	 * The name of the <code>handlePut()</code> entry point in the script.
	 */
	private String entryPointNameForPut;

	/**
	 * The name of the <code>handleDelete()</code> entry point in the script.
	 */
	private String entryPointNameForDelete;

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
	 * The {@link Writer} used by the {@link Document}.
	 */
	private Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private Writer errorWriter = new OutputStreamWriter( System.err );
}