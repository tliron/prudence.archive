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

(import
 'java.util.concurrent.Executors
 'org.restlet.Component
 'com.threecrickets.prudence.DelegatedStatusService
 'com.threecrickets.prudence.cache.ChainCache
 'com.threecrickets.prudence.cache.HazelcastCache
 'it.sauronsoftware.cron4j.Scheduler)

;
; Component
;

(def component (Component.))
(.. executable getGlobals (put "com.threecrickets.prudence.component" component))

(.. component getContext getAttributes (put "com.threecrickets.prudence.version" prudence-version))
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

(def executor (Executors/newScheduledThreadPool (+ (* (.. Runtime getRuntime (availableProcessors)) 2) 1)))
(.. component getContext getAttributes (put "com.threecrickets.prudence.executor" executor))
(def tasks [])

;
; Scheduler
;

(def scheduler (Scheduler.))
(.. component getContext getAttributes (put "com.threecrickets.prudence.scheduler" scheduler))

;
; Cache
;

(def cache (ChainCache.))
(.. cache getCaches (add (HazelcastCache.)))
(.. component getContext getAttributes (put "com.threecrickets.prudence.cache" cache))

;
; Predefined Shared Globals
;
; These will be available to your code via application.sharedGlobals.
;

(def predefined-shared-globals {})
