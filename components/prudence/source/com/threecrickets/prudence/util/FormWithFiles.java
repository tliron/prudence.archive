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

package com.threecrickets.prudence.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;

/**
 * A form that can parse {@link MediaType#MULTIPART_FORM_DATA} entities by
 * accepting file uploads. Files will appear as parameters of type
 * {@link FileParameter}.
 * 
 * @author Tal Liron
 */
public class FormWithFiles extends Form
{
	//
	// Construction
	//

	/**
	 * Construction with default system repository directory.
	 * 
	 * @param webForm
	 *        The URL encoded web form
	 */
	public FormWithFiles( Representation webForm )
	{
		this( webForm, new DiskFileItemFactory() );
	}

	/**
	 * Constructor.
	 * 
	 * @param webForm
	 *        The URL encoded web form
	 * @param sizeThreshold
	 *        The size in bytes beyond which files will be stored to disk
	 * @param repositoryDirectory
	 *        The directory in which to place uploaded files
	 */
	public FormWithFiles( Representation webForm, int sizeThreshold, File repositoryDirectory )
	{
		this( webForm, new DiskFileItemFactory( sizeThreshold, repositoryDirectory ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param webForm
	 *        The URL encoded web form
	 * @param fileItemFactory
	 *        The file item factory
	 * @throws IOException
	 */
	public FormWithFiles( Representation webForm, FileItemFactory fileItemFactory )
	{
		if( webForm.getMediaType().includes( MediaType.MULTIPART_FORM_DATA ) )
		{
			RestletFileUpload fileUpload = new RestletFileUpload( fileItemFactory );

			try
			{
				for( FileItem fileItem : fileUpload.parseRepresentation( webForm ) )
				{
					Parameter parameter;
					if( fileItem.isFormField() )
						parameter = new Parameter( fileItem.getFieldName(), fileItem.getString() );
					else
					{
						if( fileItem instanceof DiskFileItem )
						{
							File file = ( (DiskFileItem) fileItem ).getStoreLocation();
							if( file == null )
								// In memory
								parameter = new FileParameter( fileItem.getFieldName(), fileItem.get(), fileItem.getContentType(), fileItem.getSize() );
							else
								// On disk
								parameter = new FileParameter( fileItem.getFieldName(), file, fileItem.getContentType(), fileItem.getSize() );
						}
						else
							// Non-file form item
							parameter = new Parameter( fileItem.getFieldName(), fileItem.getString() );
					}

					add( parameter );
				}
			}
			catch( FileUploadException x )
			{
				x.printStackTrace();
			}
		}
		else
		{
			// Default parsing
			addAll( new Form( webForm ) );
		}
	}
}
