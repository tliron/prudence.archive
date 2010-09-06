;
; Prudence Component
;

(.execute document "defaults/instance/component/")

(import
	'com.threecrickets.prudence.cache.H2Cache
	'com.threecrickets.prudence.cache.ChainCache)

; Create an H2-database-backed cache chained after the default memory cache
(def default-cache (.. component getContext getAttributes (get "com.threecrickets.prudence.cache")))
(def chain-cache (ChainCache.))
(.. chain-cache getCaches (add default-cache))
(.. chain-cache getCaches (add (H2Cache. (str (.. document getSource getBasePath (getPath)) "/cache/prudence/prudence"))))
(.. component getContext getAttributes (put "com.threecrickets.prudence.cache" chain-cache))
