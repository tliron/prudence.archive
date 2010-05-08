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

(.. component getContext getAttributes (put "prudence.version" prudence-version))
(.. component getContext getAttributes (put "prudence.revision" prudence-revision))
(.. component getContext getAttributes (put "prudence.flavor" prudence-flavor))

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

(def executor (Executors/newFixedThreadPool (.. Runtime getRuntime (availableProcessors))))
(.. component getContext getAttributes (put "prudence.executor" executor))

;
; Cache
;

(.. component getContext getAttributes (put "com.threecrickets.prudence.cache" (InProcessMemoryCache.)))
