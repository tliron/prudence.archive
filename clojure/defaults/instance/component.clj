;
; Prudence Component
;

(import
	'java.util.concurrent.Executors
	'org.restlet.Component
	'com.threecrickets.prudence.util.DelegatedStatusService
	'com.threecrickets.prudence.cache.InProcessMemoryCache)

;
; Component
;

(def component (Component.))

(.. component getContext getAttributes (put "com.threecrickets.prudence.version" prudence-version))
(.. component getContext getAttributes (put "com.threecrickets.prudence.revision" prudence-revision))
(.. component getContext getAttributes (put "com.threecrickets.prudence.flavor" prudence-flavor))

;
; Logging
;

(.. component getLogService (setLoggerName "web-requests"))

;
; StatusService
;

(.. component (setStatusService (DelegatedStatusService.)))

;
; Executor
;

(def executor (Executors/newFixedThreadPool (+ (* (.. Runtime getRuntime (availableProcessors)) 2) 1)))
(.. component getContext getAttributes (put "com.threecrickets.prudence.executor" executor))

;
; Cache
;

(.. component getContext getAttributes (put "com.threecrickets.prudence.cache" (InProcessMemoryCache.)))
