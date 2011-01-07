#
# Prudence Component
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

$document.execute 'defaults/instance/component/'

import com.threecrickets.prudence.cache.H2Cache
import com.threecrickets.prudence.cache.ChainCache

# Create an H2-database-backed cache chained after the default memory cache
$default_cache = $component.context.attributes['com.threecrickets.prudence.cache']
$chain_cache = ChainCache.new
$chain_cache.caches.add $default_cache
$chain_cache.caches.add H2Cache.new($document.source.base_path.path + '/cache/prudence/prudence')
$component.context.attributes['com.threecrickets.prudence.cache'] = $chain_cache
