;
; Prudence Application
;

(import
	'org.restlet.data.Reference
	'org.restlet.data.MediaType
	'com.threecrickets.prudence.util.DelegatedStatusService
 'com.threecrickets.prudence.util.PrudenceTaskCollector)

;
; Settings
;

(execute-or-default (str application-base-path "/settings/") "defaults/application/settings/")
;
; Application
;

(execute-or-default (str application-base-path "/application/") "defaults/application/application/")

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

(execute-or-default (str application-base-path "/routing/") "defaults/application/routing/")

;
; Logging
;

(.. application-instance getContext (setLogger application-logger-name))

;
; Predefined Globals
;

(.putAll attributes predefined-globals)

;
; Tasks
;

(def scheduler-document-source (DocumentFileSource. (str application-base-path tasks_base_path) tasks-default-document "clj" tasks-minimum-time-between-validity-checks))
(def task-collector (PrudenceTaskCollector. (File. (str application-base-path "/crontab")) scheduler-document-source language-manager 'clojure' true (.getContent application-instance)))
(.addTaskCollector scheduler task-collector)
