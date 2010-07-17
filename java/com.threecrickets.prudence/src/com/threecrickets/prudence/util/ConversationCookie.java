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

package com.threecrickets.prudence.util;

import java.util.ArrayList;
import java.util.Collection;

import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

/**
 * A modifiable extension of a regular {@link Cookie}. Tracks modifications, and
 * upon calling {@link #save()} stores them as a {@link CookieSetting}, likely
 * in the {@link Response}. Also supports cookie deletion via {@link #remove()}.
 * 
 * @author Tal Liron
 */
public class ConversationCookie extends Cookie
{
	//
	// Static operations
	//

	/**
	 * Returns a new conversation cookie instance if the cookie doesn't exist
	 * yet, or the existing cookie if it does.
	 * 
	 * @param name
	 *        The cookie name
	 * @param cookieSettings
	 *        The cookie settings (will be used only if we need to create a new
	 *        cookie)
	 * @param conversationCookies
	 *        The conversation cookies
	 * @return A new conversation cookie or the existing conversation cookie
	 */
	public static ConversationCookie createCookie( String name, Series<CookieSetting> cookieSettings, Collection<ConversationCookie> conversationCookies )
	{
		// Look for existing conversation cookie
		for( ConversationCookie conversationCookie : conversationCookies )
			if( conversationCookie.getName().equals( name ) )
				return conversationCookie;

		// Not found, so create a new one
		ConversationCookie conversationCookie = new ConversationCookie( name, cookieSettings );
		conversationCookies.add( conversationCookie );
		return conversationCookie;
	}

	/**
	 * Creates a collection of conversation cookies based on a resource.
	 * 
	 * @param resource
	 *        The resource (response might be altered as we change our
	 *        conversation cookies)
	 * @return A collection of conversation cookies
	 */
	public static Collection<ConversationCookie> wrapCookies( ServerResource resource )
	{
		return wrapCookies( resource.getCookies(), resource.getCookieSettings() );
	}

	/**
	 * Creates a collection of conversation cookies based on existing cookies
	 * and settings.
	 * 
	 * @param cookies
	 *        The cookies
	 * @param cookieSettings
	 *        The cookie settings (might be altered as we change our
	 *        conversation cookies)
	 * @return A collection of conversation cookies
	 */
	public static Collection<ConversationCookie> wrapCookies( Series<Cookie> cookies, Series<CookieSetting> cookieSettings )
	{
		Collection<ConversationCookie> conversationCookies = new ArrayList<ConversationCookie>();
		for( Cookie cookie : cookies )
			conversationCookies.add( new ConversationCookie( cookie, cookieSettings ) );
		return conversationCookies;
	}

	//
	// Construction
	//

	/**
	 * Construction based on existing cookie.
	 * 
	 * @param cookie
	 *        The cookies
	 * @param cookieSettings
	 *        The cookie settings (might be altered as we change our
	 *        conversation cookie)
	 */
	public ConversationCookie( Cookie cookie, Series<CookieSetting> cookieSettings )
	{
		super( cookie.getVersion(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain() );
		originalCookie = cookie;
		this.cookieSettings = cookieSettings;
	}

	/**
	 * Construction.
	 * 
	 * @param name
	 *        The cookie name
	 * @param cookieSettings
	 *        The cookie settings (might be altered as we change our
	 *        conversation cookie)
	 */
	public ConversationCookie( String name, Series<CookieSetting> cookieSettings )
	{
		originalCookie = null;
		this.cookieSettings = cookieSettings;
		changed = true;
		super.setName( name );
	}

	//
	// Attributes
	//

	/**
	 * The comment.
	 * 
	 * @return The comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * The comment.
	 * 
	 * @param comment
	 *        The comment
	 */
	public void setComment( String comment )
	{
		changed = true;
		this.comment = comment;
	}

	/**
	 * Max age. Use 0 to immediately discard an existing cookie. Use -1 to
	 * discard the cookie at the end of the session (default).
	 * 
	 * @return The max age in seconds
	 */
	public int getMaxAge()
	{
		return maxAge;
	}

	/**
	 * Max age. Use 0 to immediately discard an existing cookie. Use -1 to
	 * discard the cookie at the end of the session (default).
	 * 
	 * @param maxAge
	 *        The max age in seconds
	 */
	public void setMaxAge( int maxAge )
	{
		changed = true;
		this.maxAge = maxAge;
	}

	/**
	 * Whether the cookie is secure.
	 * 
	 * @return Whether the cookie is secure
	 */
	public boolean isSecure()
	{
		return secure;
	}

	/**
	 * Whether the cookie is secure.
	 * 
	 * @param secure
	 *        Whether the cookie is secure
	 */
	public void seSecure( boolean secure )
	{
		changed = true;
		this.secure = secure;
	}

	/**
	 * Whether access is restricted.
	 * 
	 * @return Whether access is restricted
	 */
	public boolean isAccessRestricted()
	{
		return accessRestricted;
	}

	/**
	 * Whether access is restricted.
	 * 
	 * @param accessRestricted
	 *        Whether access is restricted
	 */
	public void setAccessRestricted( boolean accessRestricted )
	{
		changed = true;
		this.accessRestricted = accessRestricted;
	}

	/**
	 * The original cookie, if there was one.
	 * 
	 * @return The original cookie or null
	 */
	public Cookie getOriginalCookie()
	{
		return originalCookie;
	}

	//
	// Operations
	//

	/**
	 * Saves changes, if any were made, as a unique cookie setting.
	 */
	public boolean save()
	{
		CookieSetting changes = getChanges();
		if( changes != null )
		{
			cookieSettings.removeAll( changes.getName() );
			cookieSettings.add( changes );
			return true;
		}

		return false;
	}

	/**
	 * Sets the cookie for deletion if it already exists (equivalent to saving
	 * with maxAge=0), or cancels a new cookie.
	 */
	public void remove()
	{
		if( originalCookie != null )
		{
			setMaxAge( 0 );
			save();
		}
		else
			cookieSettings.removeAll( getName() );
	}

	//
	// Cookie
	//

	@Override
	public void setVersion( int version )
	{
		checkForChange = true;
		super.setVersion( version );
	}

	@Override
	public void setName( String name )
	{
		throw new UnsupportedOperationException( "Can't change name of a conversation cookie; create a new cookie instead" );
	}

	@Override
	public void setValue( String value )
	{
		checkForChange = true;
		super.setValue( value );
	}

	@Override
	public void setPath( String path )
	{
		checkForChange = true;
		super.setPath( path );
	}

	@Override
	public void setDomain( String domain )
	{
		checkForChange = true;
		super.setDomain( domain );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The original cookie or null.
	 */
	private final Cookie originalCookie;

	/**
	 * The cookie settings.
	 */
	private final Series<CookieSetting> cookieSettings;

	/**
	 * Whether we've changed our cookie since the original (always true if we
	 * have no original).
	 */
	private boolean changed = false;

	/**
	 * Whether a change might have happened.
	 */
	private boolean checkForChange = false;

	/**
	 * The comment.
	 */
	private String comment;

	/**
	 * The max age in seconds.
	 */
	private int maxAge = -1;

	/**
	 * Whether access is restricted.
	 */
	private boolean accessRestricted = false;

	/**
	 * Whether the cookie is secure.
	 */
	private boolean secure = false;

	/**
	 * Creates a cookie setting if the cookie has been changed from the
	 * original.
	 * 
	 * @return The cookie setting
	 */
	private CookieSetting getChanges()
	{
		if( !changed && checkForChange )
			changed = getVersion() != originalCookie.getVersion() || !getValue().equals( originalCookie.getValue() ) || !getPath().equals( originalCookie.getPath() ) || !getDomain().equals( originalCookie.getDomain() );

		if( changed )
		{
			CookieSetting cookieSetting = new CookieSetting( getVersion(), getName(), getValue(), getPath(), getDomain(), comment, maxAge, secure );
			cookieSetting.setAccessRestricted( accessRestricted );
			return cookieSetting;
		}

		return null;
	}
}
