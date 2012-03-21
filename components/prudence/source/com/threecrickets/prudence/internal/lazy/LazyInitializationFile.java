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

package com.threecrickets.prudence.internal.lazy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.fileupload.RestletFileUpload;

/**
 * A PHP-style $_FILE map.
 * <p>
 * See PHP's <a
 * href="http://www.php.net/manual/en/reserved.variables.php">predefined
 * variables</a>.
 * 
 * @author Tal Liron
 */
public class LazyInitializationFile extends LazyInitializationMap<String, Map<String, Object>>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        The request
	 */
	public LazyInitializationFile( Request request, FileItemFactory fileItemFactory )
	{
		super( new HashMap<String, Map<String, Object>>() );
		this.request = request;
		fileUpload = new RestletFileUpload( fileItemFactory );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// LazyInitializationMap
	//

	@Override
	protected void initialize()
	{
		if( ( request.getMethod().equals( Method.POST ) || request.getMethod().equals( Method.PUT ) ) && request.isEntityAvailable() && request.getEntity().getMediaType().includes( MediaType.MULTIPART_FORM_DATA ) )
		{
			try
			{
				for( FileItem fileItem : fileUpload.parseRequest( request ) )
				{
					if( fileItem.isFormField() )
						formFields.put( fileItem.getFieldName(), fileItem.getString() );
					else
						map.put( fileItem.getFieldName(), Collections.unmodifiableMap( createFileItemMap( fileItem ) ) );
				}
			}
			catch( FileUploadException x )
			{
				x.printStackTrace();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Fields that are not uploaded files will go here.
	 */
	protected final Map<String, String> formFields = new HashMap<String, String>();

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The request.
	 */
	private final Request request;

	/**
	 * The request parser for file uploads.
	 */
	private final RestletFileUpload fileUpload;

	/**
	 * Creates a PHP-style item in the $_FILE map.
	 * 
	 * @param fileItem
	 *        The file itme
	 * @return A PHP-style $_FILE item
	 */
	private static Map<String, Object> createFileItemMap( FileItem fileItem )
	{
		Map<String, Object> exposedFileItem = new HashMap<String, Object>();
		exposedFileItem.put( "name", fileItem.getName() );
		exposedFileItem.put( "type", fileItem.getContentType() );
		exposedFileItem.put( "size", fileItem.getSize() );
		if( fileItem instanceof DiskFileItem )
		{
			DiskFileItem diskFileItem = (DiskFileItem) fileItem;
			exposedFileItem.put( "tmp_name", diskFileItem.getStoreLocation().getAbsolutePath() );
		}
		// exposedFileItem.put("error", );
		return exposedFileItem;
	}
}