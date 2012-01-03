
var chainCache = new com.threecrickets.prudence.cache.ChainCache()
component.context.attributes.put('com.threecrickets.prudence.cache', chainCache)

Sincerity.Container.executeAll('backends')
