;
; Prudence Component
;

(import
	'java.lang.System
	'java.io.FileNotFoundException
	'java.util.logging.LogManager
	'org.restlet.Component
	'com.threecrickets.prudence.util.DelegatedStatusService
)

(defn include-or-default
	(
		[name default]
		(try
			(.. document getContainer (include name))
			(catch FileNotFoundException _
				(.. document getContainer (include
					(if (nil? default)
						(str "defaults/" name)
						default
					))
				)
			)
		)
	)
	(
		[name]
		(include-or-default name nil)
	)
)

;
; Welcome
;

(println "Prudence 1.0 for Clojure.")

;
; Component
;

(def component (Component.))

;
; Logging
;

; log4j: This is our actual logging engine
(try
	(import 'org.apache.log4j.PropertyConfigurator)
	(org.apache.log4j.PropertyConfigurator/configure "conf/logging.conf")
	(catch Exception _ nil)
) 

; JULI: Remove any pre-existing configuration
(. (LogManager/getLogManager) reset)

; JULI: Bridge to SLF4J, which will use log4j as its engine 
(try
	(import 'org.slf4j.bridge.SLF4JBridgeHandler)
	(org.slf4j.bridge.SLF4JBridgeHandler/install)
	(catch Exception _ nil)
) 

; Set Restlet to use SLF4J, which will use log4j as its engine
(System/setProperty "org.restlet.engine.loggerFacadeClass" "org.restlet.ext.slf4j.Slf4jLoggerFacade")

; Velocity logging
(System/setProperty "com.sun.script.velocity.properties" "conf/velocity.conf")

; Web requests
(.. component getLogService (setLoggerName "web-requests"))

;
; StatusService
;

(.. component (setStatusService (DelegatedStatusService.)))

;
; Routing
;

(include-or-default "component/routing")

;
; Clients
;

(include-or-default "component/clients")

;
; Servers
;

(include-or-default "component/servers")

;
; Start
;

(.. component getContext getAttributes (put "applications" applications))
(.start component)
