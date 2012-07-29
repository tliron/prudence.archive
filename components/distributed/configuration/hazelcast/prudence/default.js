
config.instanceName = 'com.threecrickets.prudence'

var map = new MapConfig()
map.name = 'com.threecrickets.prudence.cache'
map.backupCount = 1
map.evictionPolicy = 'LFU'
map.evictionPercentage = 25
config.addMapConfig(map)

map = new MapConfig()
map.name = 'com.threecrickets.prudence.cacheTagMap'
map.backupCount = 1
config.addMapConfig(map)

map = new MapConfig()
map.name = 'com.threecrickets.prudence.distributedGlobals'
map.backupCount = 1
map.evictionPolicy = 'LFU'
map.evictionPercentage = 25
config.addMapConfig(map)
