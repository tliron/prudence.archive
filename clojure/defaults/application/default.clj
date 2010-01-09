;
; Prudence Application
;

(import
	'org.restlet.data.Reference
	'org.restlet.data.MediaType
	'com.threecrickets.prudence.util.DelegatedStatusService)

(def tasks [])

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

;
; Logging
;

(.. application getContext (setLogger application-logger-name))

;
; Additional/Override Runtime Attributes
;

(.putAll attributes runtime-attributes)


;
; Tasks
;

(doseq [task tasks]
	(.submit executor task))
