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

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;

/**
 * A <a href="http://memcached.org/">memcached</a>-backed cache over the <a
 * herf="http://code.google.com/p/spymemcached/">spymemcached</a> client
 * library.
 * <p>
 * By default, waits for all memcached commands to complete. However, you can
 * turn this feature off to increase throughput at the cost of allowing for
 * inconsistent cache states.
 * <p>
 * Note that {@link #reset()} (which causes memcached flushing) works only if
 * you are the sole client of the memcached cluster.
 * 
 * @author Tal Liron
 */
public class MemcachedCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction, with single node on the local host, default port 11211
	 * using the binary protocol; waits for completion; no tag prefix.
	 * 
	 * @throws IOException
	 */
	public MemcachedCache() throws IOException
	{
		this( "127.0.0.1:11211" );
	}

	/**
	 * Construction: using the binary protocol; waits for completion; no tag
	 * prefix.
	 * 
	 * @param nodes
	 *        The node list (space-separated IP addresses or hostnames with port
	 *        specifications after colon)
	 * @throws IOException
	 */
	public MemcachedCache( String nodes ) throws IOException
	{
		this( nodes, true, true, "" );
	}

	/**
	 * Construction, with node list, using the binary protocol.
	 * 
	 * @param nodes
	 *        The node list (space-separated IP addresses or hostnames with port
	 *        specifications after colon)
	 * @param waitForCompletion
	 *        Whether to wait for all commands to be completed
	 * @param soleClient
	 *        Whether we are the sole clients of the memcached cluster
	 * @param tagPrefix
	 *        Prefix to be added to tag keys
	 * @throws IOException
	 * @see AddrUtil#getAddresses(String)
	 */
	public MemcachedCache( String nodes, boolean waitForCompletion, boolean soleClient, String tagPrefix ) throws IOException
	{
		this( new MemcachedClient( new BinaryConnectionFactory(), AddrUtil.getAddresses( nodes ) ), waitForCompletion, soleClient, tagPrefix );
	}

	/**
	 * Construction.
	 * 
	 * @param memcached
	 *        The memcached client
	 * @param waitForCompletion
	 *        Whether to wait for all commands to be completed
	 * @param soleClient
	 *        Whether we are the sole clients of the memcached cluster
	 * @param tagPrefix
	 *        Prefix to be added to tag keys
	 */
	public MemcachedCache( MemcachedClient memcached, boolean waitForCompletion, boolean soleClient, String tagPrefix )
	{
		this.memcached = memcached;
		this.waitForCompletion = waitForCompletion;
		this.soleClient = soleClient;
		this.tagPrefix = tagPrefix;
	}

	//
	// Cache
	//

	public void store( String key, Iterable<String> tags, CacheEntry entry )
	{
		if( debug )
			System.out.println( "Store: " + key + " " + tags );

		Object theEntry = entry;

		// Use tagged cache entry if we're tagged
		if( ( tags != null ) && ( tags.iterator().hasNext() ) )
			theEntry = new TaggedCacheEntry( entry, tags );

		Future<Boolean> stored = memcached.set( key, (int) ( entry.getExpirationDate().getTime() / 1000 ), theEntry );
		if( waitForCompletion )
		{
			try
			{
				stored.get();
			}
			catch( InterruptedException x )
			{
				// Restore interrupt status
				Thread.currentThread().interrupt();
			}
			catch( ExecutionException x )
			{
			}
		}
	}

	public CacheEntry fetch( String key )
	{
		try
		{
			Object entry = memcached.get( key );
			CacheEntry cacheEntry = null;
			if( entry != null )
			{
				String[] tags = null;
				long timestamp = 0;
				if( entry instanceof TaggedCacheEntry )
				{
					TaggedCacheEntry taggedCacheEntry = (TaggedCacheEntry) entry;
					cacheEntry = taggedCacheEntry.entry;
					tags = taggedCacheEntry.tags;
					timestamp = taggedCacheEntry.timestamp;
				}
				else
					cacheEntry = (CacheEntry) entry;

				Date now = new Date();

				if( tags != null )
				{
					for( String tag : tags )
					{
						Long tagTimestamp = (Long) memcached.get( tagPrefix + tag );
						if( tagTimestamp != null )
						{
							if( tagTimestamp > timestamp )
							{
								// Tag is newer, so this entry should be
								// considered invalid

								if( debug )
									System.out.println( "Invalidated tagged entry: " + key + ", tag: " + tag );

								Future<Boolean> deleted = memcached.delete( key );
								if( waitForCompletion )
								{
									try
									{
										deleted.get();
									}
									catch( InterruptedException x )
									{
										// Restore interrupt status
										Thread.currentThread().interrupt();
									}
									catch( ExecutionException x )
									{
									}
								}

								cacheEntry = null;

								break;
							}
						}
					}
				}

				if( ( cacheEntry != null ) && now.after( cacheEntry.getExpirationDate() ) )
				{
					// This should never happen with memcached, but it doesn't
					// hurt to double check.

					if( debug )
						System.out.println( "Stale entry: " + key );

					Future<Boolean> deleted = memcached.delete( key );
					if( waitForCompletion )
					{
						try
						{
							deleted.get();
						}
						catch( InterruptedException x )
						{
							// Restore interrupt status
							Thread.currentThread().interrupt();
						}
						catch( ExecutionException x )
						{
						}
					}

					cacheEntry = null;
				}
			}

			if( debug )
			{
				if( cacheEntry != null )
					System.out.println( "Fetched: " + key );
				else
					System.out.println( "Did not fetch: " + key );
			}

			return cacheEntry;
		}
		catch( OperationTimeoutException x )
		{
			if( debug )
				System.out.println( "memcached is down" );

			return null;
		}
	}

	public void invalidate( String tag )
	{
		Future<Boolean> set = memcached.set( tagPrefix + tag, 0, System.currentTimeMillis() );
		if( waitForCompletion )
		{
			try
			{
				set.get();
			}
			catch( InterruptedException x )
			{
				// Restore interrupt status
				Thread.currentThread().interrupt();
			}
			catch( ExecutionException x )
			{
			}
		}
	}

	public void prune()
	{
		// Handled automatically by memcached
	}

	public void reset()
	{
		// Only allow flushing if we are the sole clients of the cluster
		if( soleClient )
		{
			Future<Boolean> flushed = memcached.flush();
			if( waitForCompletion )
			{
				try
				{
					flushed.get();
				}
				catch( InterruptedException x )
				{
					// Restore interrupt status
					Thread.currentThread().interrupt();
				}
				catch( ExecutionException x )
				{
				}
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The memcached client.
	 */
	private final MemcachedClient memcached;

	/**
	 * Whether we are the sole clients of the memcached cluster.
	 */
	private final boolean soleClient;

	/**
	 * Whether to wait for all commands to be completed.
	 */
	private final boolean waitForCompletion;

	/**
	 * Prefix to be added to tag keys.
	 */
	private final String tagPrefix;

	/**
	 * Whether to print debug messages to standard out.
	 */
	private volatile boolean debug = false;
}
