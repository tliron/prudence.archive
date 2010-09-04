;
; Prudence Application
;

(import
 'org.restlet.data.Reference
 'org.restlet.data.MediaType
 'com.threecrickets.prudence.DelegatedStatusService
 'com.threecrickets.prudence.ApplicationTaskCollector)

;
; Settings
;

(execute-or-default (str application-base "/settings/") "defaults/application/settings/")
;
; Application
;

(execute-or-default (str application-base "/application/") "defaults/application/application/")

(.setName application-instance application-name)
(.setDescription application-instance application-description)
(.setAuthor application-instance application-author)
(.setOwner application-instance application-owner)

;
; StatusService
;

(.setStatusService application-instance (DelegatedStatusService. (if show-debug-on-error show-source-code-url nil)))
(.. application-instance getStatusService (setDebugging show-debug-on-error))
(.. application-instance getStatusService (setHomeRef (Reference. application-home-url)))
(.. application-instance getStatusService (setContactEmail application-contact-email))

;
; MetaData
;

(.. application-instance getMetadataService (addExtension "php" MediaType/TEXT_HTML))

;
; Routing
;

(execute-or-default (str application-base "/routing/") "defaults/application/routing/")

;
; Logging
;

(.. application-instance getContext (setLogger application-logger-name))

;
; Predefined Globals
;

(.putAll application-globals predefined-globals)

;
; Tasks
;

(def tasks-document-source (DocumentFileSource. (str application-base-path tasks-base-path) tasks-default-name "clj" (.longValue tasks-minimum-time-between-validity-checks)))
(.put application-globals "com.threecrickets.prudence.ApplicationTask.languageManager" language-manager)
(.put application-globals "com.threecrickets.prudence.ApplicationTask.defaultLanguageTag" "clojure")
(.put application-globals "com.threecrickets.prudence.ApplicationTask.defaultName" tasks-default-name)
(.put application-globals "com.threecrickets.prudence.ApplicationTask.documentSource" tasks-document-source)
(.addTaskCollector scheduler (ApplicationTaskCollector. (File. (str application-base-path "/crontab")) application-instance))
