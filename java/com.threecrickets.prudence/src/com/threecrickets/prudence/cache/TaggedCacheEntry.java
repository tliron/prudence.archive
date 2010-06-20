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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;

/**
 * A {@link CacheEntry} decorated with tagging information.
 * 
 * @author Tal Liron
 */
public class TaggedCacheEntry implements Externalizable
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param entry
	 *        The cache entry
	 * @param tags
	 *        The tags
	 */
	public TaggedCacheEntry( CacheEntry entry, Iterable<String> tags )
	{
		this.entry = entry;
		int size = 0;
		for( Iterator<String> i = tags.iterator(); i.hasNext(); i.next() )
			size++;
		this.tags = new String[size];
		int i = 0;
		for( String tag : tags )
			this.tags[i++] = tag;
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Construction.
	 * 
	 * @param entry
	 *        The cache entry
	 * @param tags
	 *        The tags
	 */
	public TaggedCacheEntry( CacheEntry entry, Collection<String> tags )
	{
		this.entry = entry;
		int size = tags.size();
		this.tags = new String[size];
		int i = 0;
		for( String tag : tags )
			this.tags[i++] = tag;
		timestamp = System.currentTimeMillis();
	}

	//
	// Attributes
	//

	/**
	 * The creation timestamp.
	 */
	public long timestamp;

	/**
	 * The cache entry.
	 */
	public CacheEntry entry;

	/**
	 * The tags.
	 */
	public String[] tags;

	//
	// Externalizable
	//

	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
	{
		timestamp = in.readLong();
		int size = in.readInt();
		tags = new String[size];
		for( int i = 0; i < size; i++ )
			tags[i] = in.readUTF();
		entry = new CacheEntry();
		entry.readExternal( in );
	}

	public void writeExternal( ObjectOutput out ) throws IOException
	{
		out.writeLong( timestamp );
		out.writeInt( tags.length );
		for( String tag : tags )
			out.writeUTF( tag );
		entry.writeExternal( out );
	}

	/**
	 * Warning: This constructor has been made public for deserialization only
	 * and should never be manually called!
	 */
	public TaggedCacheEntry()
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
