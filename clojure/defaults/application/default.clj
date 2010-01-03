;
; Prudence Application
;

(import
	'javax.script.ScriptEngineManager
	'org.restlet.data.Reference
	'org.restlet.data.MediaType
	'com.threecrickets.scripturian.file.DocumentFileSource
	'com.threecrickets.prudence.util.DelegatedStatusService
)

;
; Settings
;

(include-or-default (str application-base-path "/settings") "defaults/application/settings")
;
; Application
;

(include-or-default (str application-base-path "/application") "defaults/application/application")

(.setName application application-name)
(.setDescription application application-description)
(.setAuthor application application-author)
(.setOwner application application-owner)

;
; StatusService
;

(.setStatusService application (DelegatedStatusService.))
(.. application getStatusService (setDebugging show-debug-on-error))
(.. application getStatusService (setHomeRef (Reference. application-home-url)))
(.. application getStatusService (setContactEmail application-contact-email))

;
; MetaData
;

(.. application getMetadataService (addExtension "php" MediaType/TEXT_HTML))

;
; Routing
;

(include-or-default (str application-base-path "/routing") "defaults/application/routing")
;(System/exit 0)

;
; Logging
;

(.. application getContext (setLogger application-logger-name))

;
; Configuration
;

(def attributes (.. application getContext (getAttributes)))

(def script-engine-manager (ScriptEngineManager.))

; DelegatedResource

(.put attributes "com.threecrickets.prudence.DelegatedResource.engineManager" script-engine-manager)
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultEngineName" "Clojure")
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultName" resource-default-name)
(.put attributes "com.threecrickets.prudence.DelegatedResource.documentSource"
	(DocumentFileSource. (str application-base-path resource-base-path) resource-default-name (.longValue resource-minimum-time-between-validity-checks)))
(.put attributes "com.threecrickets.prudence.DelegatedResource.sourceViewable" resource-source-viewable)

; GeneratedTextResource

(.put attributes "com.threecrickets.prudence.GeneratedTextResource.engineManager" script-engine-manager)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultEngineName" "Clojure")
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultName" dynamic-web-default-document)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.documentSource"
	 (DocumentFileSource. (str application-base-path dynamic-web-base-path) dynamic-web-default-document (.longValue dynamic-web-minimum-time-between-validity-checks)))
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.sourceViewable" dynamic-web-source-viewable)

; Additional runtime attributes

(.putAll attributes runtime-attributes)
