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

package com.threecrickets.prudence.internal.attributes;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;

import com.threecrickets.prudence.DelegatedResource;

/**
 * @author Tal Liron
 */
public class DelegatedResourceAttributes extends ResourceContextualAttributes
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 */
	public DelegatedResourceAttributes( DelegatedResource resource )
	{
		super( resource );
	}

	//
	// Attributes
	//

	/**
	 * The name of the <code>handleInit()</code> entry point in the executable.
	 * Defaults to "handleInit".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForInit</code> in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleInit()</code> entry point
	 */
	public String getEntryPointNameForInit()
	{
		if( entryPointNameForInit == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForInit = (String) attributes.get( prefix + ".entryPointNameForInit" );

			if( entryPointNameForInit == null )
				entryPointNameForInit = "handleInit";
		}

		return entryPointNameForInit;
	}

	/**
	 * The name of the <code>handleGet()</code> entry point in the executable.
	 * Defaults to "handleGet".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForGet</code> in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleGet()</code> entry point
	 */
	public String getEntryPointNameForGet()
	{
		if( entryPointNameForGet == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForGet = (String) attributes.get( prefix + ".entryPointNameForGet" );

			if( entryPointNameForGet == null )
				entryPointNameForGet = "handleGet";
		}

		return entryPointNameForGet;
	}

	/**
	 * The name of the <code>handleGetInfo()</code> entry point in the
	 * executable. Defaults to "handleGetInfo".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForGetInfo</code> in the application's
	 * {@link Context}.
	 * 
	 * @return The name of the <code>handleGetInfo()</code> entry point
	 */
	public String getEntryPointNameForGetInfo()
	{
		if( entryPointNameForGetInfo == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForGetInfo = (String) attributes.get( prefix + ".entryPointNameForGetInfo" );

			if( entryPointNameForGetInfo == null )
				entryPointNameForGetInfo = "handleGetInfo";
		}

		return entryPointNameForGetInfo;
	}

	/**
	 * The name of the <code>handleOptions()</code> entry point in the
	 * executable. Defaults to "handleOptions".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForOptions</code> in the application's
	 * {@link Context}.
	 * 
	 * @return The name of the <code>handleOptions()</code> entry point
	 */
	public String getEntryPointNameForOptions()
	{
		if( entryPointNameForOptions == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForOptions = (String) attributes.get( prefix + ".entryPointNameForOptions" );

			if( entryPointNameForOptions == null )
				entryPointNameForOptions = "handleOptions";
		}

		return entryPointNameForOptions;
	}

	/**
	 * The name of the <code>handlePost()</code> entry point in the executable.
	 * Defaults to "handlePost".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForPost</code> in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handlePost()</code> entry point
	 */
	public String getEntryPointNameForPost()
	{
		if( entryPointNameForPost == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForPost = (String) attributes.get( prefix + ".entryPointNameForPost" );

			if( entryPointNameForPost == null )
				entryPointNameForPost = "handlePost";
		}

		return entryPointNameForPost;
	}

	/**
	 * The name of the <code>handlePut()</code> entry point in the executable.
	 * Defaults to "handlePut".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForPut</code> in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handlePut()</code> entry point
	 */
	public String getEntryPointNameForPut()
	{
		if( entryPointNameForPut == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForPut = (String) attributes.get( prefix + ".entryPointNameForPut" );

			if( entryPointNameForPut == null )
				entryPointNameForPut = "handlePut";
		}

		return entryPointNameForPut;
	}

	/**
	 * The name of the <code>handleDelete()</code> entry point in the
	 * executable. Defaults to "handleDelete".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>entryPointNameForDelete</code> in the application's {@link Context}.
	 * 
	 * @return The name of the <code>handleDelete()</code> entry point
	 */
	public String getEntryPointNameForDelete()
	{
		if( entryPointNameForDelete == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			entryPointNameForDelete = (String) attributes.get( prefix + ".entryPointNameForDelete" );

			if( entryPointNameForDelete == null )
				entryPointNameForDelete = "handleDelete";
		}

		return entryPointNameForDelete;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The name of the <code>handleInit()</code> entry point in the executable.
	 */
	private String entryPointNameForInit;

	/**
	 * The name of the <code>handleGet()</code> entry point in the executable.
	 */
	private String entryPointNameForGet;

	/**
	 * The name of the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 */
	private String entryPointNameForGetInfo;

	/**
	 * The name of the <code>handleOptions()</code> entry point in the
	 * executable.
	 */
	private String entryPointNameForOptions;

	/**
	 * The name of the <code>handlePost()</code> entry point in the executable.
	 */
	private String entryPointNameForPost;

	/**
	 * The name of the <code>handlePut()</code> entry point in the executable.
	 */
	private String entryPointNameForPut;

	/**
	 * The name of the <code>handleDelete()</code> entry point in the
	 * executable.
	 */
	private String entryPointNameForDelete;
}
