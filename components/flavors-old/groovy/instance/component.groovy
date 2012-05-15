//
// Prudence Component
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.execute('/defaults/instance/component/')

import com.threecrickets.prudence.cache.H2Cache
import com.threecrickets.prudence.cache.ChainCache

// Create an H2-database-backed cache chained after the default memory cache
defaultCache = componentInstance.context.attributes['com.threecrickets.prudence.cache']
chainCache = new ChainCache(defaultCache, new H2Cache(document.source.basePath.path + '/cache/prudence/prudence'))
componentInstance.context.attributes['com.threecrickets.prudence.cache'] = chainCache
