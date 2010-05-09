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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Request;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.ext.fileupload.RestletFileUpload;

import com.threecrickets.prudence.internal.LazyInitializationMap;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.exception.ExecutionException;

/**
 * An execution controller that exposes PHP-style <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a> ("superglobals") to executables.
 * 
 * @author Tal Liron
 */
public class PhpExecutionController implements ExecutionController
{
	//
	// ExecutionController
	//

	public void initialize( ExecutionContext executionContext ) throws ExecutionException
	{
		Request request = Request.getCurrent();

		Map<String, String> exposedGet = new LazyInitializationExposedGet( new HashMap<String, String>(), request );
		Map<String, String> exposedPost = new LazyInitializationExposedPost( new HashMap<String, String>(), request );
		Map<String, String> exposedCookie = new LazyInitializationExposedCookie( new HashMap<String, String>(), request );
		Map<String, String> exposedRequest = new LazyInitializationExposedRequest( new HashMap<String, String>(), exposedGet, exposedPost, exposedCookie );
		Map<String, Map<String, Object>> exposedFile = new LazyInitializationExposedFile( new HashMap<String, Map<String, Object>>(), request );

		// Note that our maps will only contain the last parameter in case of
		// duplicates. This is PHP's behavior.

		executionContext.getExposedVariables().put( "_GET", Collections.unmodifiableMap( exposedGet ) );
		executionContext.getExposedVariables().put( "_POST", Collections.unmodifiableMap( exposedPost ) );
		executionContext.getExposedVariables().put( "_COOKIE", Collections.unmodifiableMap( exposedCookie ) );
		executionContext.getExposedVariables().put( "_REQUEST", Collections.unmodifiableMap( exposedRequest ) );
		executionContext.getExposedVariables().put( "_FILE", Collections.unmodifiableMap( exposedFile ) );
	}

	public void release( ExecutionContext executionContext )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Map<String, Object> createFileItemMap( FileItem fileItem )
	{
		Map<String, Object> exposedFileItem = new HashMap<String, Object>();
		exposedFileItem.put( "name", fileItem.getName() );
		exposedFileItem.put( "type", fileItem.getContentType() );
		exposedFileItem.put( "size", fileItem.getSize() );
		// exposedFileItem.put("tmp_name", );
		// exposedFileItem.put("error", fileItem);
		return exposedFileItem;
	}

	private static class LazyInitializationExposedGet extends LazyInitializationMap<String, String>
	{
		public LazyInitializationExposedGet( Map<String, String> map, Request request )
		{
			super( map );
			this.request = request;
		}

		@Override
		protected void initialize()
		{
			if( request.getMethod().equals( Method.GET ) )
			{
				Form form = request.getResourceRef().getQueryAsForm();
				for( Parameter parameter : form )
					map.put( parameter.getName(), parameter.getValue() );
			}
		}

		private final Request request;
	}

	private static class LazyInitializationExposedCookie extends LazyInitializationMap<String, String>
	{
		public LazyInitializationExposedCookie( Map<String, String> map, Request request )
		{
			super( map );
			this.request = request;
		}

		@Override
		protected void initialize()
		{
			for( Cookie cookie : request.getCookies() )
				map.put( cookie.getName(), cookie.getValue() );
		}

		private final Request request;
	}

	private static class LazyInitializationExposedPost extends LazyInitializationMap<String, String>
	{
		public LazyInitializationExposedPost( Map<String, String> map, Request request )
		{
			super( map );
			this.request = request;
		}

		@Override
		protected void initialize()
		{
			if( request.getMethod().equals( Method.POST ) && request.isEntityAvailable() )
			{
				Form form = new Form( request.getEntity() );
				for( Parameter parameter : form )
					map.put( parameter.getName(), parameter.getValue() );
			}
		}

		private final Request request;
	}

	private static class LazyInitializationExposedRequest extends LazyInitializationMap<String, String>
	{
		public LazyInitializationExposedRequest( Map<String, String> map, Map<String, String> exposedGet, Map<String, String> exposedPost, Map<String, String> exposedCookie )
		{
			super( map );
			this.exposedGet = exposedGet;
			this.exposedPost = exposedPost;
			this.exposedCookie = exposedCookie;
		}

		@Override
		protected void initialize()
		{
			map.putAll( exposedGet );
			map.putAll( exposedPost );
			map.putAll( exposedCookie );
		}

		private final Map<String, String> exposedGet;

		private final Map<String, String> exposedPost;

		private final Map<String, String> exposedCookie;
	}

	private static class LazyInitializationExposedFile extends LazyInitializationMap<String, Map<String, Object>>
	{
		public LazyInitializationExposedFile( Map<String, Map<String, Object>> map, Request request )
		{
			super( map );
			this.request = request;
		}

		@Override
		protected void initialize()
		{
			try
			{
				RestletFileUpload fileUpload = new RestletFileUpload();
				for( FileItem fileItem : fileUpload.parseRequest( request ) )
					map.put( fileItem.getFieldName(), Collections.unmodifiableMap( createFileItemMap( fileItem ) ) );
			}
			catch( FileUploadException x )
			{
				x.printStackTrace();
			}
		}

		private final Request request;
	}
}
