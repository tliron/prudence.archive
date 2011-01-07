//
// Prudence Component
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.execute('defaults/instance/component/')

importClass(
	com.threecrickets.prudence.cache.H2Cache,
	com.threecrickets.prudence.cache.ChainCache)

// Create an H2-database-backed cache chained after the default memory cache
var defaultCache = component.context.attributes.get('com.threecrickets.prudence.cache')
var chainCache = new ChainCache()
chainCache.caches.add(defaultCache)
chainCache.caches.add(new H2Cache(document.source.basePath.path + '/cache/prudence/prudence'))
component.context.attributes.put('com.threecrickets.prudence.cache', chainCache)
