#
# Prudence Component
#

$document.execute 'defaults/instance/component/'

import com.threecrickets.prudence.cache.H2Cache
import com.threecrickets.prudence.cache.ChainCache

# Create an H2-database-backed cache chained after the default memory cache
$default_cache = $component.context.attributes['com.threecrickets.prudence.cache']
$chain_cache = ChainCache.new
$chain_cache.caches.add $default_cache
$chain_cache.caches.add H2Cache.new('cache/prudence/prudence')
$component.context.attributes['com.threecrickets.prudence.cache'] = $chain_cache
