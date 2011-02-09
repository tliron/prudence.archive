package com.threecrickets.prudence.internal;

import java.util.concurrent.ConcurrentMap;

import org.restlet.resource.ServerResource;

public class ResourceContextualAttributes extends NonVolatileContextualAttributes
{
	//
	// Construction
	//

	public ResourceContextualAttributes( ServerResource resource )
	{
		super( resource.getClass().getCanonicalName() );
		this.resource = resource;
	}

	//
	// ContextualAttributes
	//

	@Override
	public ConcurrentMap<String, Object> getAttributes()
	{
		if( attributes == null )
			attributes = resource.getContext().getAttributes();
		return attributes;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final ServerResource resource;

	/**
	 * The attributes.
	 */
	private ConcurrentMap<String, Object> attributes;
}
