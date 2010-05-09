#
# Prudence Component
#

document.execute('defaults/instance/component/')

from com.threecrickets.prudence.cache import H2Cache, ChainCache

# Create an H2-database-backed cache chained after the default memory cache
default_cache = component.context.attributes['com.threecrickets.prudence.cache']
chain_cache = ChainCache()
chain_cache.caches.add(default_cache)
chain_cache.caches.add(H2Cache('cache/prudence/prudence'))
component.context.attributes['com.threecrickets.prudence.cache'] = chain_cache
