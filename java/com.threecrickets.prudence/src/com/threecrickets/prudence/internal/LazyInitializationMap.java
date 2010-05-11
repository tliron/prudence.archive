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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map wrapper that initializes once and only once upon first access.
 * 
 * @author Tal Liron
 * @param <K>
 *        The key class
 * @param <V>
 *        The value class
 */
public abstract class LazyInitializationMap<K, V> implements Map<K, V>
{
	//
	// Construction
	//

	/**
	 * Construction.
	 * 
	 * @param map
	 *        The map to initialize
	 */
	public LazyInitializationMap( Map<K, V> map )
	{
		this.map = map;
	}

	//
	// Operations
	//

	/**
	 * Makes sure we initialize once and only once.
	 */
	public void validateInitialized()
	{
		synchronized( initializedLock )
		{
			if( !initialized )
			{
				initialize();
				initialized = true;
			}
		}
	}

	//
	// Map
	//

	public void clear()
	{
		validateInitialized();
		map.clear();
	}

	public boolean containsKey( Object key )
	{
		validateInitialized();
		return map.containsKey( key );
	}

	public boolean containsValue( Object value )
	{
		validateInitialized();
		return map.containsValue( value );
	}

	public Set<Map.Entry<K, V>> entrySet()
	{
		validateInitialized();
		return map.entrySet();
	}

	public V get( Object key )
	{
		validateInitialized();
		return map.get( key );
	}

	public boolean isEmpty()
	{
		validateInitialized();
		return map.isEmpty();
	}

	public Set<K> keySet()
	{
		validateInitialized();
		return map.keySet();
	}

	public V put( K key, V value )
	{
		validateInitialized();
		return map.put( key, value );
	}

	public void putAll( Map<? extends K, ? extends V> t )
	{
		validateInitialized();
		map.putAll( t );
	}

	public V remove( Object key )
	{
		validateInitialized();
		return map.remove( key );
	}

	public int size()
	{
		validateInitialized();
		return map.size();
	}

	public Collection<V> values()
	{
		validateInitialized();
		return map.values();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Do the initialization of {@link #map}.
	 */
	protected abstract void initialize();

	/**
	 * The actual, wrapped map.
	 */
	protected final Map<K, V> map;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Whether we've initialized.
	 * 
	 * @see #initializedLock
	 */
	private boolean initialized;

	/**
	 * For synchronization of {@link #initialized}.
	 */
	private Object initializedLock = new Object();
}