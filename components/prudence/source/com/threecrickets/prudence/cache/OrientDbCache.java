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

package com.threecrickets.prudence.cache;

import java.util.logging.Logger;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;

/**
 * An <a href="http://www.orientdb.org/">OrientDB</a>-backed cache.
 * 
 * @author Tal Liron
 */
public class OrientDbCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Constructor with global database document pool.
	 */
	public OrientDbCache()
	{
		this( ODatabaseDocumentPool.global() );
	}

	/**
	 * Constructor.
	 * 
	 * @param databaseDocumentPool
	 *        The database document pool.
	 */
	public OrientDbCache( ODatabaseDocumentPool databaseDocumentPool )
	{
		this.databaseDocumentPool = databaseDocumentPool;
	}

	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
	}

	public CacheEntry fetch( String key )
	{
		return null;
	}

	public void invalidate( String tag )
	{
	}

	public void prune()
	{
	}

	public void reset()
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The logger.
	 */
	private final Logger logger = Logger.getLogger( this.getClass().getCanonicalName() );

	/**
	 * The database document pool.
	 */
	private final ODatabaseDocumentPool databaseDocumentPool;
}
