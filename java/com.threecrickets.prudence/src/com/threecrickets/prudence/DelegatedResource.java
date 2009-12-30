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
import java.util.Date;
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
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.internal.ExposedContainerForDelegatedResource;
import com.threecrickets.prudence.internal.PrudenceUtils;
import com.threecrickets.prudence.util.SourceFormatter;
import com.threecrickets.prudence.util.SyntaxHighlighterSyntaxFormatter;
import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource;
import com.threecrickets.scripturian.ScriptletController;
import com.threecrickets.scripturian.DocumentSource.DocumentDescriptor;

/**
 * A Restlet resource which delegates functionality to a Scripturian
 * {@link Document} with well-defined entry points. The entry points must be
 * global functions, closures, or whatever other technique the language engine
 * uses to make entry points available to Java. They entry points are:
 * <ul>
 * <li><code>handleInit()</code>: This function is called when the resource is
 * initialized. We will use it set general characteristics for the resource.</li>
 * <li><code>handleGet()</code>: This function is called for the GET verb, which
 * is expected to behave as a logical "read" of the resource's state. The
 * expectation is that it return one representation, out of possibly many, of
 * the resource's state. Returned values can be of any explicit sub-class of
 * {@link Representation}. If you return an integer, it will be set as the
 * response status code and a null representation will be returned to the
 * client. Other types will be automatically converted to string representation
 * using the client's requested media type and character set. These, and the
 * language of the representation (defaulting to null), can be read and changed
 * via <code>container.mediaType</code>, <code>container.characterSet</code>,
 * and <code>container.language</code>. Additionally, you can use
 * <code>container.variant</code> to interrogate the client's provided list of
 * supported languages and encoding.</li>
 * <li><code>handleGetInfo()</code>: This optional function is called, if you
 * defined it, instead of <code>handleGet()</code> during conditional
 * processing. Rather of returning a full-blown representation of your data, it
 * returns a lightweight {@link RepresentationInfo}, which usefully includes the
 * modification date and tag. In cases where constructing the full-blown
 * representation is costly, implementing <code>handleGetInfo()</code> is a
 * great way to improve the performance of your resource. Note, though, that it
 * is only useful if you properly set modification dates and/or tags in both
 * <code>handleGet()</code> and <code>handleGetInfo()</code>. Returned values
 * can be explicit sub-classes of {@link RepresentationInfo} (which includes
 * {@link Representation}), {@link Date}, for only specifying a modification
 * date, or {@link Tag}, for only specifying the tag.</li>
 * <li><code>handlePost()</code>: This function is called for the POST verb,
 * which is expected to behave as a logical "update" of the resource's state.
 * The expectation is that <code>container.entity</code> represents an update to
 * the state, that will affect future calls to <code>handleGet()</code>. As
 * such, it may be possible to accept logically partial representations of the
 * state. You may optionally return a representation, in the same way as
 * <code>handleGet()</code>. Because many languages functions return the last
 * statement's value by default, you must explicitly return a null if you do not
 * want to return a representation to the client.</li>
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
 * <li><code>handleOptions()</code>: This function is called for the OPTIONS
 * verb. It is not widely used in HTTP.</li>
 * <p>
 * Names of these entry point can be configured via attributes in the
 * application's {@link Context}. See {@link #getEntryPointNameForInit()},
 * {@link #getEntryPointNameForGet()}, {@link #getEntryPointNameForGetInfo()},
 * {@link #getEntryPointNameForPost()}, {@link #getEntryPointNameForPut()},
 * {@link #getEntryPointNameForDelete()} and
 * {@link #getEntryPointNameForOptions()}.
 * <p>
 * Before using this resource, make sure to configure a valid source in the
 * application's {@link Context}; see {@link #getDocumentSource()}. This source
 * is accessible from the script itself, via <code>prudence.source</code>.
 * <p>
 * Note that the composite script's output is sent to the system's standard
 * output. Most likely, you will not want to output anything from the script.
 * However, this redirection is provided as a convenience, which may be useful
 * for certain debugging situations.
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
 * </ul>
 * Read-only attributes:
 * <ul>
 * <li><code>prudence.entity</code>: The {@link Representation} of an entity
 * provided with this request. Available only in <code>handlePost()</code> and
 * <code>handlePut()</code>. Note that
 * <li><code>prudence.expirationDate</code>: Smart clients can use this optional
 * value to cache results and avoid unnecessary requests. Most useful in
 * conjunction with <code>getInfo()</code>.</li> <code>prudence.variant</code>
 * is identical to <code>prudence.entity</code> when available.</li>
 * <li><code>prudence.httpTag</code>: See <code>prudence.tag</code>.</li>
 * <li><code>prudence.resource</code>: The instance of this resource. Acts as a
 * "this" reference for scriptlets. For example, during a call to
 * <code>handleInit()</code>, this can be used to change the characteristics of
 * the resource. Otherwise, you can use it to access the request and response.</li>
 * <li><code>prudence.modificationDate</code>: Smart clients can use this
 * optional value to cache results and avoid unnecessary requests. Most useful
 * in conjunction with <code>getInfo()</code>. Note that you need to use
 * {@link Date} instances here. Use <code>prudence.modificationTimestamp</code>
 * to access this value as a timestamp (long).</li>
 * <li><code>prudence.modificationTimestamp</code>: See
 * <code>prudence.modificationDate</code>.</li>
 * <li><code>prudence.statusCode</code>: A convenient way to set the response
 * status code. This is equivalent to setting
 * <code>prudence.response.status</code> using {@link Status#valueOf(int)}.</li>
 * <li><code>prudence.source</code>: The source used for the document; see
 * {@link #getDocumentSource()}.</li>
 * <li><code>prudence.tag</code>: Smart clients can use this optional value to
 * cache results and avoid unnecessary requests. Most useful in conjunction with
 * <code>getInfo()</code>. Note that you need to use {@link Tag} instances here.
 * Use <code>prudence.httpTag</code> to access this value as an HTTP ETag
 * string.</li>
 * <li><code>prudence.variant</code>: The {@link Variant} of this request.
 * Useful for interrogating the client's preferences. This is available only in
 * <code>handleGet()</code>, <code>handlePost()</code> and
 * <code>handlePut()</code>.</li>
 * <li><code>prudence.variants</code>: A map of possible variants or media types
 * supported by this resource. You should initialize this during a call to
 * <code>handleInit()</code>. Values for the map can be {@link MediaType}
 * constants, explicit {@link Variant} instances (in which case these variants
 * will be returned immediately for their media type without calling the entry
 * point), or a {@link List} containing both media types and variants. Use map
 * key {@link Method#ALL} to indicate support for all methods.</li>
 * </ul>
 * Modifiable attributes:
 * <ul>
 * <li><code>prudence.mediaType</code>: The {@link MediaType} that will be used
 * if you return an arbitrary type for <code>handleGet()</code>,
 * <code>handlePost()</code> and <code>handlePut()</code>. Defaults to what the
 * client requested (in <code>prudence.variant</code>).</li>
 * <li><code>prudence.characterSet</code>: The {@link CharacterSet} that will be
 * used if you return an arbitrary type for <code>handleGet()</code>,
 * <code>handlePost()</code> and <code>handlePut()</code>. Defaults to what the
 * client requested (in <code>prudence.variant</code>), or to the value of
 * {@link #getDefaultCharacterSet()} if the client did not specify it.</li>
 * <li><code>prudence.language</code>: The {@link Language} that will be used if
 * you return an arbitrary type for <code>handleGet()</code>,
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
 * <li><code>com.threecrickets.prudence.DelegatedResource.containerName</code>:
 * The name of the global variable with which to access the container. Defaults
 * to "prudence". See {@link #getContainerName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}. See
 * {@link #getDefaultCharacterSet()}.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.defaultName:</code>
 * {@link String}, defaults to "default.script". See {@link #getDefaultName()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultEngineName:</code>
 * {@link String}, defaults to "js". See {@link #getDefaultEngineName()}.</li>
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
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo:</code>
 * {@link String}, defaults to "handleGetInfo". See
 * {@link #getEntryPointNameForGetInfo()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit:</code>
 * {@link String}, defaults to "handleInit". See
 * {@link #getEntryPointNameForInit()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions:</code>
 * {@link String}, defaults to "handleOptions". See
 * {@link #getEntryPointNameForOptions()}.</li>
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
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.scriptletController:</code>
 * {@link ScriptletController}. See {@link #getScriptletController()}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.engineManager:</code>
 * {@link ScriptEngineManager}, defaults to a new instance. See
 * {@link #getEngineManager()}.</li>
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
		if( writer == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			writer = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedResource.writer" );

			if( writer == null )
				writer = new OutputStreamWriter( System.out );
		}

		return writer;
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
		if( errorWriter == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			errorWriter = (Writer) attributes.get( "com.threecrickets.prudence.DelegatedResource.errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.out );
		}

		return errorWriter;
	}

	/**
	 * The name of the global variable with which to access the container.
	 * Defaults to "prudence".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.containerName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The container name
	 */
	public String getContainerName()
	{
		if( containerName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			containerName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.containerName" );

			if( containerName == null )
				containerName = "prudence";
		}

		return containerName;
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
		if( defaultCharacterSet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultCharacterSet = (CharacterSet) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "default".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultName</code> in
	 * the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName()
	{
		if( defaultName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultName" );

			if( defaultName == null )
				defaultName = "default";
		}

		return defaultName;
	}

	/**
	 * The default script engine name to be used if the script doesn't specify
	 * one. Defaults to "js".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.defaultEngineName</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The default script engine name
	 */
	public String getDefaultEngineName()
	{
		if( defaultEngineName == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			defaultEngineName = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.defaultEngineName" );

			if( defaultEngineName == null )
				defaultEngineName = "js";
		}

		return defaultEngineName;
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
		if( entryPointNameForInit == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForInit = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForInit" );

			if( entryPointNameForInit == null )
				entryPointNameForInit = "handleInit";
		}

		return entryPointNameForInit;
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
		if( entryPointNameForGet == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForGet = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForGet" );

			if( entryPointNameForGet == null )
				entryPointNameForGet = "handleGet";
		}

		return entryPointNameForGet;
	}

	/**
	 * The name of the <code>handleGetInfo()</code> entry point in the script.
	 * Defaults to "handleGetInfo".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleGetInfo()</code> entry point
	 */
	public String getEntryPointNameForGetInfo()
	{
		if( entryPointNameForGetInfo == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForGetInfo = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo" );

			if( entryPointNameForGetInfo == null )
				entryPointNameForGetInfo = "handleGetInfo";
		}

		return entryPointNameForGetInfo;
	}

	/**
	 * The name of the <code>handleOptions()</code> entry point in the script.
	 * Defaults to "handleOptions".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleOptions()</code> entry point
	 */
	public String getEntryPointNameForOptions()
	{
		if( entryPointNameForOptions == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForOptions = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions" );

			if( entryPointNameForOptions == null )
				entryPointNameForOptions = "handleOptions";
		}

		return entryPointNameForOptions;
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
		if( entryPointNameForPost == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForPost = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForPost" );

			if( entryPointNameForPost == null )
				entryPointNameForPost = "handlePost";
		}

		return entryPointNameForPost;
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
		if( entryPointNameForPut == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForPut = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForPut" );

			if( entryPointNameForPut == null )
				entryPointNameForPut = "handlePut";
		}

		return entryPointNameForPut;
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
		if( entryPointNameForDelete == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			entryPointNameForDelete = (String) attributes.get( "com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete" );

			if( entryPointNameForDelete == null )
				entryPointNameForDelete = "handleDelete";
		}

		return entryPointNameForDelete;
	}

	/**
	 * An optional {@link ScriptletController} to be used with the document.
	 * Useful for adding your own global variables to the document.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.scriptletController</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script context controller or null if none used
	 */
	public ScriptletController getScriptletController()
	{
		if( scriptletController == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			scriptletController = (ScriptletController) attributes.get( "com.threecrickets.prudence.DelegatedResource.scriptletController" );
		}

		return scriptletController;
	}

	/**
	 * The {@link ScriptEngineManager} used to create the script engines. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.engineManager</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The script engine manager
	 */
	public ScriptEngineManager getEngineManager()
	{
		if( engineManager == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			engineManager = (ScriptEngineManager) attributes.get( "com.threecrickets.prudence.DelegatedResource.engineManager" );

			if( engineManager == null )
			{
				engineManager = new ScriptEngineManager();

				ScriptEngineManager existing = (ScriptEngineManager) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.engineManager", engineManager );
				if( existing != null )
					engineManager = existing;
			}
		}

		return engineManager;
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
		if( documentSource == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			documentSource = (DocumentSource<Document>) attributes.get( "com.threecrickets.prudence.DelegatedResource.documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute com.threecrickets.prudence.DelegatedResource.documentSource must be set in context to use ScriptResource" );
		}

		return documentSource;
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
		if( allowCompilation == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			allowCompilation = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.allowCompilation" );

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
	 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable</code>
	 * in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of document source code
	 */
	public boolean isSourceViewable()
	{
		if( sourceViewable == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			sourceViewable = (Boolean) attributes.get( "com.threecrickets.prudence.DelegatedResource.sourceViewable" );

			if( sourceViewable == null )
				sourceViewable = false;
		}

		return sourceViewable;
	}

	/**
	 * An optional {@link SourceFormatter} to use for showing source code.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.DelegatedResource.sourceFormatter</code>
	 * in the application's {@link Context}.
	 * 
	 * @return The source formatter or null
	 * @see #isSourceViewable()
	 */
	public SourceFormatter getSourceFormatter()
	{
		if( sourceFormatter == null )
		{
			ConcurrentMap<String, Object> attributes = getContext().getAttributes();
			sourceFormatter = (SourceFormatter) attributes.get( "com.threecrickets.prudence.DelegatedResource.sourceFormatter" );

			if( sourceFormatter == null )
			{
				sourceFormatter = new SyntaxHighlighterSyntaxFormatter();

				SourceFormatter existing = (SourceFormatter) attributes.putIfAbsent( "com.threecrickets.prudence.DelegatedResource.sourceFormatter", sourceFormatter );
				if( existing != null )
					sourceFormatter = existing;
			}
		}

		return sourceFormatter;
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
		container.invoke( getEntryPointNameForInit() );
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
			String name = PrudenceUtils.getRemainingPart( request, getDefaultName() );
			try
			{
				DocumentDescriptor<Document> documentDescriptor = getDocumentSource().getDocumentDescriptor( name );
				SourceFormatter sourceFormatter = getSourceFormatter();
				if( sourceFormatter != null )
					return sourceFormatter.formatSource( documentDescriptor.getText(), name, documentDescriptor.getTag() );
				else
					return new StringRepresentation( documentDescriptor.getText() );
			}
			catch( IOException x )
			{
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, x );
			}
		}
		else
		{
			Object r = container.invoke( getEntryPointNameForGet() );
			return getRepresentation( container, r );
		}
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the script.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForGetInfo()
	 */
	@Override
	public RepresentationInfo getInfo() throws ResourceException
	{
		return getInfo( null );
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the script.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForGetInfo()
	 */
	@Override
	public RepresentationInfo getInfo( Variant variant ) throws ResourceException
	{
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants(), null, variant );
		Object r = container.invoke( getEntryPointNameForGetInfo() );
		return getRepresentationInfo( container, r );
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
		return getRepresentation( container, r );
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
		return getRepresentation( container, r );
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

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the script.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForOptions()
	 */
	@Override
	public Representation options() throws ResourceException
	{
		return options( null );
	}

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the script.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 * @see #getEntryPointNameForOptions()
	 */
	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		ExposedContainerForDelegatedResource container = new ExposedContainerForDelegatedResource( this, getVariants(), variant );
		Object r = container.invoke( getEntryPointNameForOptions() );
		return getRepresentation( container, r );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The {@link ScriptEngineManager} used to create the script engines for the
	 * scripts.
	 */
	private volatile ScriptEngineManager engineManager;

	/**
	 * Whether or not compilation is attempted for script engines that support
	 * it.
	 */
	private volatile Boolean allowCompilation;

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
	 * The name of the <code>handleInit()</code> entry point in the script.
	 */
	private volatile String entryPointNameForInit;

	/**
	 * The name of the <code>handleGet()</code> entry point in the script.
	 */
	private volatile String entryPointNameForGet;

	/**
	 * The name of the <code>handleGetInfo()</code> entry point in the script.
	 */
	private volatile String entryPointNameForGetInfo;

	/**
	 * The name of the <code>handleOptions()</code> entry point in the script.
	 */
	private volatile String entryPointNameForOptions;

	/**
	 * The name of the <code>handlePost()</code> entry point in the script.
	 */
	private volatile String entryPointNameForPost;

	/**
	 * The name of the <code>handlePut()</code> entry point in the script.
	 */
	private volatile String entryPointNameForPut;

	/**
	 * The name of the <code>handleDelete()</code> entry point in the script.
	 */
	private volatile String entryPointNameForDelete;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	private volatile Boolean sourceViewable;

	/**
	 * The {@link Writer} used by the {@link Document}.
	 */
	private volatile Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	private volatile Writer errorWriter = new OutputStreamWriter( System.err );

	/**
	 * The name of the global variable with which to access the container.
	 */
	private volatile String containerName;

	/**
	 * The source code formatter.
	 */
	private volatile SourceFormatter sourceFormatter;

	/**
	 * Constant.
	 */
	private static final String SOURCE = "source";

	/**
	 * Constant.
	 */
	private static final String TRUE = "true";

	/**
	 * Returns a representation based on the object. If the object is not
	 * already a representation, creates a new representation based on the
	 * container's attributes.
	 * 
	 * @param container
	 *        The container
	 * @param object
	 *        An object
	 * @return A representation
	 */
	private Representation getRepresentation( ExposedContainerForDelegatedResource container, Object object )
	{
		if( object == null )
			return null;
		else if( object instanceof Representation )
			return (Representation) object;
		else if( object instanceof Integer )
		{
			getResponse().setStatus( Status.valueOf( (Integer) object ) );
			return null;
		}
		else
		{
			Representation representation = new StringRepresentation( object.toString(), container.getMediaType(), container.getLanguage(), container.getCharacterSet() );
			representation.setTag( container.getTag() );
			representation.setExpirationDate( container.getExpirationDate() );
			representation.setModificationDate( container.getModificationDate() );
			return representation;
		}
	}

	/**
	 * Returns a representation info based on the object. If the object is not
	 * already a representation info, creates a new representation info based on
	 * the container's attributes.
	 * 
	 * @param container
	 *        The container
	 * @param object
	 *        An object
	 * @return A representation info
	 */
	public RepresentationInfo getRepresentationInfo( ExposedContainerForDelegatedResource container, Object object )
	{
		if( object == null )
			return null;
		else if( object instanceof RepresentationInfo )
			return (RepresentationInfo) object;
		else if( object instanceof Date )
			return new RepresentationInfo( container.getMediaType(), (Date) object );
		else if( object instanceof Number )
			return new RepresentationInfo( container.getMediaType(), new Date( ( (Number) object ).longValue() ) );
		else if( object instanceof Tag )
			return new RepresentationInfo( container.getMediaType(), (Tag) object );
		else if( object instanceof String )
			return new RepresentationInfo( container.getMediaType(), Tag.parse( (String) object ) );
		else
			throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "cannot convert " + object.getClass().toString() + " to a RepresentationInfo" );
	}
}