package com.threecrickets.prudence.internal;

import java.util.concurrent.ConcurrentMap;

import org.restlet.resource.ServerResource;

public class ResourceContextualAttributes<R extends ServerResource> extends VolatileContextualAttributes
{
	//
	// Construction
	//

	public ResourceContextualAttributes( R resource )
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
		return resource.getContext().getAttributes();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final R resource;
}
