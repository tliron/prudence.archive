;
; Prudence Application
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
 'org.restlet.data.Reference
 'org.restlet.data.MediaType
 'com.threecrickets.prudence.DelegatedStatusService
 'com.threecrickets.prudence.ApplicationTaskCollector
 'com.threecrickets.prudence.util.LoggingUtil
 'com.threecrickets.prudence.service.ApplicationService)

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

(.. application-instance getContext (setLogger (LoggingUtil/getRestletLogger application-logger-name)))

;
; Predefined Globals
;

(.putAll application-globals predefined-globals)

;
; Handlers
;

(def handlers-document-source (DocumentFileSource. (str application-base handlers-base-path) (str application-base-path handlers-base-path) documents-default-name "clj" (.longValue minimum-time-between-validity-checks)))
(.put application-globals "com.threecrickets.prudence.DelegatedHandler.documentSource" handlers-document-source)

;
; Tasks
;

(def tasks-document-source (DocumentFileSource. (str application-base tasks-base-path) (str application-base-path tasks-base-path) documents-default-name "clj" (.longValue minimum-time-between-validity-checks)))
(.put application-globals "com.threecrickets.prudence.ApplicationTask.documentSource" tasks-document-source)

(.addTaskCollector scheduler (ApplicationTaskCollector. (File. (str application-base-path "/crontab")) application-instance))

;
; Common Configurations
;

(defn configure-common [prefix]
 (.put application-globals (str prefix ".languageManager") language-manager)
 (.put application-globals (str prefix ".defaultName") documents-default-name)
 (.put application-globals (str prefix ".defaultLanguageTag") "clojure")
 (.put application-globals (str prefix ".librariesDocumentSource") libraries-document-source)
 (.put application-globals (str prefix ".commonLibrariesDocumentSource") common-libraries-document-source)
 (.put application-globals (str prefix ".fileUploadSizeThreshold") file-upload-size-threshold)
 (.put application-globals (str prefix ".sourceViewable") source-viewable))

(configure-common "com.threecrickets.prudence.GeneratedTextResource")
(configure-common "com.threecrickets.prudence.DelegatedResource")
(configure-common "com.threecrickets.prudence.DelegatedHandler")
(configure-common "com.threecrickets.prudence.ApplicationTask")

;
; ApplicationService
;

(def application-service (ApplicationService. application-instance))
