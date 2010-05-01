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

		Map<String, String> exposedGet = new HashMap<String, String>();
		Map<String, String> exposedPost = new HashMap<String, String>();
		Map<String, String> exposedCookie = new HashMap<String, String>();
		Map<String, String> exposedRequest = new HashMap<String, String>();
		Map<String, Map<String, Object>> exposedFile = new HashMap<String, Map<String, Object>>();

		// Note that our maps will only contain the last parameter in case of
		// duplicates. This is PHP's behavior.

		if( request.getMethod().equals( Method.GET ) )
		{
			Form form = request.getResourceRef().getQueryAsForm();
			for( Parameter parameter : form )
				exposedGet.put( parameter.getName(), parameter.getValue() );
		}
		else if( request.getMethod().equals( Method.POST ) )
		{
			if( request.isEntityAvailable() )
			{
				Form form = new Form( request.getEntity() );
				for( Parameter parameter : form )
					exposedPost.put( parameter.getName(), parameter.getValue() );
			}

			try
			{
				RestletFileUpload fileUpload = new RestletFileUpload();
				for( FileItem fileItem : fileUpload.parseRequest( request ) )
					exposedFile.put( fileItem.getFieldName(), Collections.unmodifiableMap( createFileItemMap( fileItem ) ) );
			}
			catch( FileUploadException x )
			{
				x.printStackTrace();
			}
		}

		for( Cookie cookie : request.getCookies() )
			exposedCookie.put( cookie.getName(), cookie.getValue() );

		exposedRequest.putAll( exposedGet );
		exposedRequest.putAll( exposedPost );
		exposedRequest.putAll( exposedCookie );

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
}
