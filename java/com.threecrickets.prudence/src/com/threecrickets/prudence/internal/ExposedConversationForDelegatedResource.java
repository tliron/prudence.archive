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

package com.threecrickets.prudence.internal;

import org.restlet.data.CharacterSet;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.DelegatedResource;

/**
 * @author Tal Liron
 */
public class ExposedConversationForDelegatedResource extends ExposedConversationBase<DelegatedResource>
{
	//
	// Construction
	//

	public ExposedConversationForDelegatedResource( DelegatedResource resource, Representation entity, Variant variant, CharacterSet defaultCharacterSet )
	{
		super( resource, entity, variant, defaultCharacterSet );
	}
}
