;
; Prudence Component
;
; Copyright 2009-2011 Three Crickets LLC.
;
; The contents of this file are subject to the terms of the LGPL version 3.0:
; http://www.opensource.org/licenses/lgpl-3.0.html
;
; Alternatively, you can obtain a royalty free commercial license with less
; limitations, transferable or non-transferable, directly from Three Crickets
; at http://threecrickets.com/
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
