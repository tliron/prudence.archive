/**
 * Copyright 2009-2010 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;

import com.threecrickets.prudence.util.CaptiveRedirector;
import com.threecrickets.prudence.util.ConversationCookie;
import com.threecrickets.prudence.util.FileParameter;
import com.threecrickets.prudence.util.FormWithFiles;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 */
public class ConversationServiceBase
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param fileUploadSizeThreshold
	 *        The size in bytes beyond which uploaded files will be stored to
	 *        disk
	 * @param fileUploadDirectory
	 *        The directory in which to place uploaded files
	 */
	public ConversationServiceBase( int fileUploadSizeThreshold, File fileUploadDirectory )
	{
		this.fileUploadSizeThreshold = fileUploadSizeThreshold;
		this.fileUploadDirectory = fileUploadDirectory;
	}

	//
	// Attributes
	//

	/**
	 * The resource reference.
	 * 
	 * @return The reference
	 */
	public Reference getReference()
	{
		Request request = getRequest();
		Reference reference = CaptiveRedirector.getCaptiveReference( request );
		if( reference == null )
			reference = request.getResourceRef();
		return reference;
	}

	/**
	 * The conversation cookies.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The conversation cookies
	 */
	public Collection<ConversationCookie> getCookies()
	{
		if( conversationCookies == null )
			conversationCookies = ConversationCookie.wrapCookies( getRequest().getCookies(), getResponse().getCookieSettings() );
		return conversationCookies;
	}

	/**
	 * Gets a conversation cookie by name.
	 * 
	 * @return The conversation cookie or null
	 */
	public ConversationCookie getCookie( String name )
	{
		for( ConversationCookie cookie : getCookies() )
			if( cookie.getName().equals( name ) )
				return cookie;
		return null;
	}

	/**
	 * Returns a new conversation cookie instance if the cookie doesn't exist
	 * yet, or the existing cookie if it does. Note that the cookie will not be
	 * saved into the response until you call {@link ConversationCookie#save()}.
	 * 
	 * @param name
	 *        The cookie name
	 * @return A new cookie or the existing cookie
	 */
	public ConversationCookie createCookie( String name )
	{
		return ConversationCookie.createCookie( name, getResponse().getCookieSettings(), getCookies() );
	}

	/**
	 * The response status.
	 * 
	 * @return The response status
	 * @see #setStatus(Status)
	 */
	public Status getStatus()
	{
		return getResponse().getStatus();
	}

	/**
	 * The response status.
	 * 
	 * @param status
	 *        The response status
	 * @see #getStatus()
	 */
	public void setStatus( Status status )
	{
		getResponse().setStatus( status );
	}

	/**
	 * The response status code.
	 * 
	 * @return The response status code
	 * @see #setStatusCode(int)
	 */
	public int getStatusCode()
	{
		return getResponse().getStatus().getCode();
	}

	/**
	 * The response status code.
	 * 
	 * @param statusCode
	 *        The response status code
	 * @see #getStatusCode()
	 */
	public void setStatusCode( int statusCode )
	{
		getResponse().setStatus( Status.valueOf( statusCode ) );
	}

	/**
	 * A shortcut to the resource request.
	 * 
	 * @return The request
	 */
	public Request getRequest()
	{
		return Request.getCurrent();
	}

	/**
	 * A shortcut to the resource response.
	 * 
	 * @return The response
	 */
	public Response getResponse()
	{
		return Response.getCurrent();
	}

	/**
	 * Checks if the request was received via the RIAP protocol
	 * 
	 * @return True if the request was received via the RIAP protocol
	 * @see LocalReference
	 */
	public boolean isInternal()
	{
		return getRequest().getResourceRef().getSchemeProtocol().equals( Protocol.RIAP );
	}

	/**
	 * Identical to {@link #isInternal()}. Supports scripting engines which
	 * don't know how to recognize the "is" getter notation, but can recognize
	 * the "get" notation.
	 * 
	 * @return True if the request was received via the RIAP protocol
	 * @see #isInternal()
	 */
	public boolean getIsInternal()
	{
		return isInternal();
	}

	/**
	 * The relative path that would reach the base URI of the application if
	 * appended to the current resource URI.
	 * 
	 * @return The relative path
	 */
	public String getPathToBase()
	{
		Reference reference = getReference();

		// Reverse relative reference
		return reference.getBaseRef().getRelativeRef( reference ).getPath();
	}

	/**
	 * The URI query as a list. Includes duplicate keys.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The query form
	 */
	public Form getQueryAll()
	{
		if( queryAll == null )
			queryAll = getRequest().getResourceRef().getQueryAsForm();
		return queryAll;
	}

	/**
	 * The URI query as a map. In the case of duplicate keys, only the last one
	 * will appear.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The query map
	 */
	public Map<String, String> getQuery()
	{
		if( query == null )
			query = getQueryAll().getValuesMap();
		return query;
	}

	/**
	 * The form, sent via POST or PUT, as a list. Includes duplicate keys.
	 * Uploaded files will appear as instances of {@link FileParameter}.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The form
	 */
	public Form getFormAll()
	{
		if( formAll == null )
		{
			if( getRequest().isEntityAvailable() )
				formAll = new FormWithFiles( getRequest().getEntity(), fileUploadSizeThreshold, fileUploadDirectory );
			else
				formAll = new Form();
		}
		return formAll;
	}

	/**
	 * The form, sent via POST or PUT, as a map. In the case of duplicate keys,
	 * only the last one will appear. Uploaded files will appear as instances of
	 * {@link FileParameter}. Other fields will be plain strings.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The form
	 */
	public Map<String, Object> getForm()
	{
		if( form == null )
		{
			form = new HashMap<String, Object>();
			for( Parameter parameter : getFormAll() )
			{
				if( parameter instanceof FileParameter )
					form.put( parameter.getName(), parameter );
				else
					form.put( parameter.getName(), parameter.getValue() );
			}
		}
		return form;
	}

	/**
	 * The request attributes.
	 * 
	 * @return The locals
	 */
	public Map<String, Object> getLocals()
	{
		return getRequest().getAttributes();
	}

	//
	// Operations
	//

	/**
	 * Abruptly ends the conversation.
	 * <p>
	 * Works by throwing a {@link RuntimeException}.
	 * 
	 * @return Always throws an exception, so nothing is ever returned (some
	 *         templating languages require a return value anyway)
	 * @see #exception(int, String)
	 */
	public boolean stop()
	{
		throw new RuntimeException( "conversation.stop was called" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	private final int fileUploadSizeThreshold;

	/**
	 * The directory in which to place uploaded files.
	 */
	private final File fileUploadDirectory;

	/**
	 * The URI query as a map.
	 */
	private Map<String, String> query;

	/**
	 * The URI query as a list.
	 */
	private Form queryAll;

	/**
	 * The form, sent via POST or PUT, as a map.
	 */
	private Form formAll;

	/**
	 * The form, sent via POST or PUT, as a list.
	 */
	private Map<String, Object> form;

	/**
	 * The conversation cookies.
	 */
	private Collection<ConversationCookie> conversationCookies;
}
