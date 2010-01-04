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

package com.threecrickets.prudence.util;

import org.restlet.Request;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.DocumentSource.DocumentDescriptor;

/**
 * @author Tal Liron
 */
public interface SourceRepresenter
{
	/**
	 * @param name
	 * @param lineNumber
	 * @param documentDescriptor
	 * @param request
	 * @return
	 * @throws ResourceException
	 */
	public Representation representSource( String name, int lineNumber, DocumentDescriptor<Document> documentDescriptor, Request request ) throws ResourceException;
}
