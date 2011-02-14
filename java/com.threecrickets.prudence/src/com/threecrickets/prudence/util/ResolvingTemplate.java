/**
 * Copyright 2009-2011 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Template;
import org.restlet.util.Resolver;

/**
 * A {@link Template} that allows control over which {@link Resolver} instances
 * it will use.
 * 
 * @author Tal Liron
 */
public class ResolvingTemplate extends Template
{
	//
	// Constants
	//

	/**
	 * @see #getMapResolverConstructor(Context)
	 * @see #setMapResolverClass(Context, Class)
	 */
	public static final String MAP_RESOLVER_CONSTRUCTOR = "com.threecrickets.prudence.util.ResolvingTemplate.mapResolverConstructor";

	/**
	 * @see #getCallResolverConstructor(Context)
	 * @see #setCallResolverClass(Context, Class)
	 */
	public static final String CALL_RESOLVER_CONSTRUCTOR = "com.threecrickets.prudence.util.ResolvingTemplate.mapResolverConstructor";

	//
	// Construction
	//

	public ResolvingTemplate( String pattern, int matchingMode, int defaultType, String defaultDefaultValue, boolean defaultRequired, boolean defaultFixed, boolean encodingVariables )
	{
		super( pattern, matchingMode, defaultType, defaultDefaultValue, defaultRequired, defaultFixed, encodingVariables );
	}

	public ResolvingTemplate( String pattern, int matchingMode, int defaultType, String defaultDefaultValue, boolean defaultRequired, boolean defaultFixed )
	{
		super( pattern, matchingMode, defaultType, defaultDefaultValue, defaultRequired, defaultFixed );
	}

	public ResolvingTemplate( String pattern, int matchingMode )
	{
		super( pattern, matchingMode );
	}

	public ResolvingTemplate( String pattern )
	{
		super( pattern );
	}

	//
	// Static attributes
	//

	public static Constructor<Resolver<?>> getMapResolverConstructor()
	{
		return getMapResolverConstructor( Application.getCurrent() );
	}

	@SuppressWarnings("unchecked")
	public static Constructor<Resolver<?>> getMapResolverConstructor( Application application )
	{
		return (Constructor<Resolver<?>>) application.getContext().getAttributes().get( MAP_RESOLVER_CONSTRUCTOR );
	}

	public static void setMapResolverClass( Application application, Class<Resolver<?>> theClass ) throws SecurityException, NoSuchMethodException
	{
		if( theClass == null )
			application.getContext().getAttributes().remove( MAP_RESOLVER_CONSTRUCTOR );
		else
			application.getContext().getAttributes().put( MAP_RESOLVER_CONSTRUCTOR, theClass.getConstructor( Map.class ) );
	}

	public static Constructor<Resolver<?>> getCallResolverConstructor()
	{
		return getCallResolverConstructor( Application.getCurrent() );
	}

	@SuppressWarnings("unchecked")
	public static Constructor<Resolver<?>> getCallResolverConstructor( Application application )
	{
		return (Constructor<Resolver<?>>) application.getContext().getAttributes().get( CALL_RESOLVER_CONSTRUCTOR );
	}

	public static void setCallResolverClass( Application application, Class<Resolver<?>> theClass ) throws SecurityException, NoSuchMethodException
	{
		if( theClass == null )
			application.getContext().getAttributes().remove( CALL_RESOLVER_CONSTRUCTOR );
		else
			application.getContext().getAttributes().put( CALL_RESOLVER_CONSTRUCTOR, theClass.getConstructor( Request.class, Response.class ) );
	}

	//
	// Attributes
	//

	/**
	 * Instances of this class will be created to resolve maps.
	 * <p>
	 * The class must have have a constructor that accepts a {@link Map}
	 * argument.
	 * 
	 * @param theClass
	 *        The class or null
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public void setMapResolverClass( Class<Resolver<?>> theClass ) throws SecurityException, NoSuchMethodException
	{
		if( theClass == null )
			mapResolverConstructor = null;
		else
			mapResolverConstructor = theClass.getConstructor( Map.class );
	}

	/**
	 * Instances of this class will be created to resolve calls.
	 * <p>
	 * The class must have have a constructor that accepts a {@link Request} and
	 * a {@link Response} as arguments.
	 * 
	 * @param theClass
	 *        The class or null
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public void setCallResolverClass( Class<Resolver<?>> theClass ) throws SecurityException, NoSuchMethodException
	{
		if( theClass == null )
			callResolverConstructor = null;
		else
			callResolverConstructor = theClass.getConstructor( Request.class, Response.class );
	}

	//
	// Template
	//

	@Override
	public String format( Map<String, ?> values )
	{
		Constructor<Resolver<?>> mapResolverConstructor = this.mapResolverConstructor;

		if( mapResolverConstructor == null )
			mapResolverConstructor = getMapResolverConstructor();

		if( mapResolverConstructor != null )
		{
			try
			{
				return format( mapResolverConstructor.newInstance( values ) );
			}
			catch( IllegalArgumentException x )
			{
				throw new RuntimeException( x );
			}
			catch( InstantiationException x )
			{
				throw new RuntimeException( x );
			}
			catch( IllegalAccessException x )
			{
				throw new RuntimeException( x );
			}
			catch( InvocationTargetException x )
			{
				throw new RuntimeException( x );
			}
		}

		return super.format( values );
	}

	@Override
	public String format( Request request, Response response )
	{
		Constructor<Resolver<?>> callResolverConstructor = this.callResolverConstructor;

		if( callResolverConstructor == null )
			callResolverConstructor = getCallResolverConstructor();

		if( callResolverConstructor != null )
		{
			try
			{
				return format( callResolverConstructor.newInstance( request, response ) );
			}
			catch( IllegalArgumentException x )
			{
				throw new RuntimeException( x );
			}
			catch( InstantiationException x )
			{
				throw new RuntimeException( x );
			}
			catch( IllegalAccessException x )
			{
				throw new RuntimeException( x );
			}
			catch( InvocationTargetException x )
			{
				throw new RuntimeException( x );
			}
		}

		return super.format( request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Used to create the map resolver.
	 */
	private volatile Constructor<Resolver<?>> mapResolverConstructor;

	/**
	 * Used to create the call resolver.
	 */
	private volatile Constructor<Resolver<?>> callResolverConstructor;
}
