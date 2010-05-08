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

package com.threecrickets.prudence.cache;

import org.h2.jdbcx.JdbcDataSource;

/**
 * An <a href="http://www.h2database.com/">H2 database</a> cache.
 * 
 * @author Tal Liron
 */
public class H2Cache extends SqlCacheBase<JdbcDataSource>
{
	//
	// Construction
	//

	/**
	 * Construction with a max entry count of 1000 entries.
	 * 
	 * @param path
	 *        The H2 database path
	 */
	public H2Cache( String path )
	{
		this( path, 1000 );
	}

	/**
	 * Construction.
	 * 
	 * @param path
	 *        The H2 database path
	 * @param maxSize
	 *        The max entry count
	 */
	public H2Cache( String path, int maxSize )
	{
		super( new JdbcDataSource() );

		getDataSource().setURL( "jdbc:h2:" + path );

		validateTables( false );
	}
}
