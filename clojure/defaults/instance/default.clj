;
; Prudence Instance
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
 'java.util.logging.LogManager 
 'java.io.File
 'java.util.concurrent.Executors
 'java.util.concurrent.CopyOnWriteArrayList
 'com.threecrickets.scripturian.document.DocumentFileSource
 'com.threecrickets.scripturian.exception.DocumentNotFoundException
 'com.threecrickets.prudence.service.ApplicationService)

;
; Common
;

(def common-libraries-document-source (DocumentFileSource. "common/libraries/" (File. (.. document getSource (getBasePath)) "common/libraries/") "default" "clj" (.longValue 5000)))
(def common-fragments-document-source (DocumentFileSource. "common/web/fragments/" (File. (.. document getSource (getBasePath)) "common/web/fragments/") "index" "clj" (.longValue 5000)))

(def common-tasks-document-sources (CopyOnWriteArrayList.))
(.add common-tasks-document-sources (DocumentFileSource. "common/tasks/" (File. (.. document getSource (getBasePath)) "common/tasks/") "default" "clj" (.longValue 5000)))
(def common-handlers-document-sources (CopyOnWriteArrayList.))
(.add common-handlers-document-sources (DocumentFileSource. "common/handlers/" (File. (.. document getSource (getBasePath)) "common/handlers/") "default" "clj" (.longValue 5000)))

(.. document getLibrarySources (add common-libraries-document-source))

;
; Utilities
;

(defn execute-or-default
	([name default]
		(try
			(.execute document name)
			(catch DocumentNotFoundException _
				(.execute document
					(if (nil? default)
						(str "/defaults/" name)
						default)))))
	([name]
		(execute-or-default name nil)))

;
; Version
;

(def prudence-version "1.1")
(def prudence-revision "-%REVISION%")
(if (= (.length prudence-revision) 1)
	(def prudence-revision ""))
(def prudence-flavor "Clojure")

;
; Welcome
;

(println (str "Prudence " prudence-version prudence-revision " for " prudence-flavor "."))

;
; Logging
;

; log4j: This is our actual logging engine
(try
	(import 'org.apache.log4j.PropertyConfigurator)
	(System/setProperty "prudence.logs" (str (.. document getSource getBasePath (getPath)) "/logs"))
	(org.apache.log4j.PropertyConfigurator/configure (str (.. document getSource getBasePath (getPath)) "/configuration/logging.conf"))
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

; Set Velocity to use log4j
(.. executable getManager getAttributes (put "velocity.runtime.log.logsystem.class" "org.apache.velocity.runtime.log.Log4JLogChute"))
(.. executable getManager getAttributes (put "velocity.runtime.log.logsystem.log4j.logger" "velocity"))

; Set spymemcached to use log4j
(System/setProperty "net.spy.log.LoggerImpl" "net.spy.memcached.compat.log.Log4JLogger")

;
; Configuration
;

; Hazelcast
(System/setProperty "hazelcast.config" "configuration/hazelcast.conf")

;
; Component
;

(execute-or-default "instance/component/")

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
; Predefined Shared Globals
;
; These will be available to your code via application.sharedGlobals.
;

(.. component getContext getAttributes (putAll predefined-shared-globals))

;
; Start
;

(.start component)

(defn print-comma-delimited [s]
	(print (apply str (interpose ", " (map str s)))))

(println "Prudence is up!")
(doseq [server (.getServers component)]
	(if-not (nil? (.getAddress server))
		(print "Listening on" (.getAddress server) "port" (.getPort server) "for ")
		(print "Listening on port" (.getPort server) "for "))
	(print-comma-delimited (.getProtocols server))
	(println "."))

;
; Scheduler
;

(.start scheduler)

;
; Tasks
;

(def fixed-executor (Executors/newFixedThreadPool (+ (* (.. Runtime getRuntime (availableProcessors)) 2) 1)))
(if-not (empty? tasks)
	(let [start-time (System/currentTimeMillis)]
		(println "Executing" (count tasks) "startup tasks...")
		(let [futures (for [task tasks] (.submit fixed-executor task))]
			(dorun (for [future futures]
        (try
          (.get future)
        	(catch Exception _))))
			(println "Finished all startup tasks in" (/ (- (System/currentTimeMillis) start-time) 1000.0) "seconds."))))

(doseq [application applications]
  (let [application-service (ApplicationService. application)]
    (.task application-service nil "/startup/" nil "initial" 0 0 false)))
