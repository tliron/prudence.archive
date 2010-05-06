;
; Prudence Component
;

(import
	'java.lang.System
	'java.io.FileNotFoundException
	'java.util.logging.LogManager
	'java.util.concurrent.Executors
	'org.restlet.Component
	'com.threecrickets.prudence.util.DelegatedStatusService)

(defn execute-or-default
	([name default]
		(try
			(.. executable getContainer (execute name))
			(catch FileNotFoundException _
				(.. executable getContainer
					(execute
						(if (nil? default)
							(str "defaults/" name)
							default))))))
	([name]
		(execute-or-default name nil)))

(def tasks [])

;
; Version
;

(def prudence-version "1.0")
(def prudence-revision "-%REVISION%")
(if (= (.length prudence-revision) 1)
	(def prudence-revision ""))
(def prudence-flavor "Clojure")

;
; Welcome
;

(println (str "Prudence " prudence-version prudence-revision " for " prudence-flavor "."))

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

; log4j: This is our actual logging engine
(try
	(import 'org.apache.log4j.PropertyConfigurator)
	(org.apache.log4j.PropertyConfigurator/configure "configuration/logging.conf")
(catch Exception _ nil)) 

; JULI: Remove any pre-existing configuration
(. (LogManager/getLogManager) reset)

; JULI: Bridge to SLF4J, which will use log4j as its engine 
(try
	(import 'org.slf4j.bridge.SLF4JBridgeHandler)
	(org.slf4j.bridge.SLF4JBridgeHandler/install)
(catch Exception _ nil)) 

; Set Restlet to use SLF4J, which will use log4j as its engine
(System/setProperty "org.restlet.engine.loggerFacadeClass" "org.restlet.ext.slf4j.Slf4jLoggerFacade")

; Velocity logging
(System/setProperty "com.sun.script.velocity.properties" "configuration/velocity.conf")

; Web requests
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
; Clients
;

(execute-or-default "instance/clients/")

;
; Routing
;

(execute-or-default "instance/routing/")

;
; Servers
;

(execute-or-default "instance/servers/")

;
; Start
;

(.. component getContext getAttributes (put "applications" applications))
(.start component)

;
; Tasks
;

(if-not (empty? tasks)
	(let [start-time (System/currentTimeMillis)]
		(println "Executing" (count tasks) "tasks...")
		(let [futures (for [task tasks] (.submit executor task))]
			(dorun (for [future futures] (.get future)))
			(println "Finished tasks in" (/ (- (System/currentTimeMillis) start-time) 1000.0) "seconds."))))
