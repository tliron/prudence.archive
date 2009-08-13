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

package com.threecrickets.prudence.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.threecrickets.prudence.ScriptedResource;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.CompositeScript;
import com.threecrickets.scripturian.CompositeScriptContext;
import com.threecrickets.scripturian.ScriptContextController;
import com.threecrickets.scripturian.ScriptSource;

/**
 * This is the type of the <code>script.container</code> variable exposed to the
 * script.
 * 
 * @author Tal Liron
 * @see ScriptedResource
 */
public class ExposedScriptedResourceContainer
{
	//
	// Construction
	//

	/**
	 * Constructs a container with no variant or entity, plain text media type,
	 * and {@link ScriptedResource#getDefaultCharacterSet()}.
	 * 
	 * @param resource
	 *        The resource
	 * @param variants
	 *        The variants of the resource
	 */
	public ExposedScriptedResourceContainer( ScriptedResource resource, Map<Method, Object> variants )
	{
		this.resource = resource;
		this.variants = variants;
		this.variant = null;
		this.entity = null;
		this.mediaType = MediaType.TEXT_PLAIN;
		this.characterSet = resource.getDefaultCharacterSet();
		this.compositeScriptContext = new CompositeScriptContext( resource.getScriptEngineManager() );
	}

	/**
	 * Constructs a container with media type and character set according to the
	 * entity representation, or
	 * {@link ScriptedResource#getDefaultCharacterSet()} if none is provided.
	 * 
	 * @param resource
	 *        The resource
	 * @param variants
	 *        The variants of the resource
	 * @param entity
	 *        The entity's representation
	 * @param variant
	 *        The request variant
	 */
	public ExposedScriptedResourceContainer( ScriptedResource resource, Map<Method, Object> variants, Representation entity, Variant variant )
	{
		this.resource = resource;
		this.variants = variants;
		this.entity = entity;
		this.variant = variant;
		this.mediaType = this.variant.getMediaType();
		this.characterSet = this.variant.getCharacterSet();
		if( this.characterSet == null )
		{
			this.characterSet = resource.getDefaultCharacterSet();
		}
		this.compositeScriptContext = new CompositeScriptContext( resource.getScriptEngineManager() );
	}

	/**
	 * Constructs a container with media type and character set according to the
	 * variant, or {@link ScriptedResource#getDefaultCharacterSet()} if none is
	 * provided.
	 * 
	 * @param resource
	 *        The resource
	 * @param variants
	 *        The variants of the resource
	 * @param variant
	 *        The variant
	 */
	public ExposedScriptedResourceContainer( ScriptedResource resource, Map<Method, Object> variants, Variant variant )
	{
		this.resource = resource;
		this.variants = variants;
		this.variant = variant;
		this.entity = null;

		if( variant != null )
		{
			this.mediaType = variant.getMediaType();
			this.characterSet = variant.getCharacterSet();
		}

		if( this.mediaType == null )
			this.mediaType = MediaType.TEXT_HTML;

		if( this.characterSet == null )
			this.characterSet = resource.getDefaultCharacterSet();

		this.compositeScriptContext = new CompositeScriptContext( resource.getScriptEngineManager() );
	}

	//
	// Attributes
	//

	/**
	 * The {@link CharacterSet} that will be used if you return an arbitrary
	 * type for <code>represent()</code>, <code>acceptRepresentation()</code>
	 * and <code>storeRepresentation()</code>. Defaults to what the client
	 * requested (in <code>container.variant</code>), or to the value of
	 * {@link ScriptedResource#getDefaultCharacterSet()} if the client did not
	 * specify it.
	 * 
	 * @return The character set
	 * @see #setCharacterSet(CharacterSet)
	 */
	public CharacterSet getCharacterSet()
	{
		return this.characterSet;
	}

	/**
	 * @param characterSet
	 *        The character set
	 * @see #getCharacterSet()
	 */
	public void setCharacterSet( CharacterSet characterSet )
	{
		this.characterSet = characterSet;
	}

	/**
	 * The {@link Language} that will be used if you return an arbitrary type
	 * for <code>represent()</code>, <code>acceptRepresentation()</code> and
	 * <code>storeRepresentation()</code>. Defaults to null.
	 * 
	 * @return The language or null if not set
	 * @see #setLanguage(Language)
	 */
	public Language getLanguage()
	{
		return this.language;
	}

	/**
	 * @param language
	 *        The language or null
	 * @see #getLanguage()
	 */
	public void setLanguage( Language language )
	{
		this.language = language;
	}

	/**
	 * The {@link MediaType} that will be used if you return an arbitrary type
	 * for <code>represent()</code>, <code>acceptRepresentation()</code> and
	 * <code>storeRepresentation()</code>. Defaults to what the client requested
	 * (in <code>container.variant</code>).
	 * 
	 * @return The media type
	 * @see #setMediaType(MediaType)
	 */
	public MediaType getMediaType()
	{
		return this.mediaType;
	}

	/**
	 * @param mediaType
	 *        The media type
	 * @see #getMediaType()
	 */
	public void setMediaType( MediaType mediaType )
	{
		this.mediaType = mediaType;
	}

	/**
	 * The instance of this resource. Acts as a "this" reference for the script.
	 * For example, during a call to <code>initializeResource()</code>, this can
	 * be used to change the characteristics of the resource. Otherwise, you can
	 * use it to access the request and response.
	 * 
	 * @return The resource
	 */
	public ScriptedResource getResource()
	{
		return this.resource;
	}

	/**
	 * The {@link Variant} of this request. Useful for interrogating the
	 * client's preferences. This is available only in <code>represent()</code>,
	 * <code>acceptRepresentation()</code> and
	 * <code>storeRepresentation()</code>.
	 * 
	 * @return The variant or null if not available
	 */
	public Variant getVariant()
	{
		return this.variant;
	}

	/**
	 * A map of possible variants or media types supported by this resource. You
	 * should initialize this during a call to <code>initializeResource()</code>
	 * . Values for the map can be {@link MediaType} constants, explicit
	 * {@link Variant} instances (in which case these variants will be returned
	 * immediately for their media type without calling the entry point), or a
	 * {@link List} containing both media types and variants. Use map key
	 * {@link Method#ALL} to indicate support for all methods.
	 * 
	 * @return The variants
	 */
	public Map<Method, Object> getVariants()
	{
		return this.variants;
	}

	/**
	 * The {@link Representation} of an entity provided with this request.
	 * Available only in <code>acceptRepresentation()</code> and
	 * <code>storeRepresentation()</code>. Note that
	 * <code>container.variant</code> is identical to
	 * <code>container.entity</code> when available.
	 * 
	 * @return The entity's representation or null if not available
	 */
	public Representation getEntity()
	{
		return this.entity;
	}

	/**
	 * The {@link ScriptSource} used to fetch and cache scripts.
	 * 
	 * @return The script source
	 */
	public ScriptSource<CompositeScript> getSource()
	{
		return this.resource.getScriptSource();
	}

	//
	// Operations
	//

	/**
	 * This powerful method allows scripts to execute other scripts in place,
	 * and is useful for creating large, maintainable applications based on
	 * scripts. Included scripts can act as a library or toolkit and can even be
	 * shared among many applications. The included script does not have to be
	 * in the same language or use the same engine as the calling script.
	 * However, if they do use the same engine, then methods, functions,
	 * modules, etc., could be shared. It is important to note that how this
	 * works varies a lot per scripting platform. For example, in JRuby, every
	 * script is run in its own scope, so that sharing would have to be done
	 * explicitly in the global scope. See the included Ruby composite script
	 * example for a discussion of various ways to do this.
	 * 
	 * @param name
	 *        The script name
	 * @throws IOException
	 * @throws ScriptException
	 */
	public void include( String name ) throws IOException, ScriptException
	{
		include( name, null );
	}

	/**
	 * As {@link #include(String)}, except that the script is not composite. As
	 * such, you must explicitly specify the name of the scripting engine that
	 * should evaluate it.
	 * 
	 * @param name
	 *        The script name
	 * @param scriptEngineName
	 *        The script engine name (if null, behaves identically to
	 *        {@link #include(String)}
	 * @throws IOException
	 * @throws ScriptException
	 */
	public void include( String name, String scriptEngineName ) throws IOException, ScriptException
	{
		ScriptSource.ScriptDescriptor<CompositeScript> scriptDescriptor = this.resource.getScriptSource().getScriptDescriptor( name );

		CompositeScript script = scriptDescriptor.getScript();
		if( script == null )
		{
			String text = scriptDescriptor.getText();

			if( scriptEngineName != null )
				text = CompositeScript.DEFAULT_DELIMITER1_START + scriptEngineName + " " + text + CompositeScript.DEFAULT_DELIMITER1_END;

			script = new CompositeScript( text, this.resource.getScriptEngineManager(), this.resource.getDefaultScriptEngineName(), this.resource.getScriptSource(), this.resource.isAllowCompilation() );
			CompositeScript existing = scriptDescriptor.setScriptIfAbsent( script );

			if( existing != null )
				script = existing;
		}

		script.run( false, this.resource.getWriter(), this.resource.getErrorWriter(), true, this.compositeScriptContext, this, this.resource.getScriptContextController() );
	}

	/**
	 * Invokes an entry point in the composite script.
	 * 
	 * @param entryPointName
	 *        Name of entry point
	 * @return Result of invocation
	 * @throws ResourceException
	 * @see {@link CompositeScript#invoke(String, Object, ScriptContextController)}
	 */
	public Object invoke( String entryPointName ) throws ResourceException
	{
		String name = ScriptUtils.getRelativePart( this.resource.getRequest(), this.resource.getDefaultName() );

		try
		{
			ScriptSource.ScriptDescriptor<CompositeScript> scriptDescriptor = this.resource.getScriptSource().getScriptDescriptor( name );

			CompositeScript script = scriptDescriptor.getScript();
			if( script == null )
			{
				String text = scriptDescriptor.getText();
				script = new CompositeScript( text, this.resource.getScriptEngineManager(), this.resource.getDefaultScriptEngineName(), this.resource.getScriptSource(), this.resource.isAllowCompilation() );
				CompositeScript existing = scriptDescriptor.setScriptIfAbsent( script );

				if( existing != null )
					script = existing;
				else
					script.run( false, this.resource.getWriter(), this.resource.getErrorWriter(), true, this.compositeScriptContext, this, this.resource.getScriptContextController() );
			}

			return script.invoke( entryPointName, this, this.resource.getScriptContextController() );
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
		catch( NoSuchMethodException x )
		{
			throw new ResourceException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The instance of this resource.
	 */
	private final ScriptedResource resource;

	/**
	 * The variants of this resource.
	 */
	private final Map<Method, Object> variants;

	/**
	 * The {@link Variant} of this request.
	 */
	private final Variant variant;

	/**
	 * The {@link Representation} of an entity provided with this request.
	 */
	private final Representation entity;

	/**
	 * The {@link MediaType} that will be used if you return an arbitrary type
	 * for <code>represent()</code>, <code>acceptRepresentation()</code> and
	 * <code>storeRepresentation()</code>.
	 */
	private MediaType mediaType;

	/**
	 * The {@link CharacterSet} that will be used if you return an arbitrary
	 * type for <code>represent()</code>, <code>acceptRepresentation()</code>
	 * and <code>storeRepresentation()</code>.
	 */
	private CharacterSet characterSet;

	/**
	 * The {@link Language} that will be used if you return an arbitrary type
	 * for <code>represent()</code>, <code>acceptRepresentation()</code> and
	 * <code>storeRepresentation()</code>.
	 */
	private Language language;

	/**
	 * The composite script context.
	 */
	private final CompositeScriptContext compositeScriptContext;
}