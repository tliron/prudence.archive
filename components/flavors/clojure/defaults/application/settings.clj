;
; Prudence Application Settings
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

;
; Information
;
; These are for administrative purposes only.
;

;(def application-name "Prudence Application") ; Defaults to the application directory name
(def application-description "This is a Prudence application.")
(def application-author "Anonymous")
(def application-owner "Public Domain")
(def application-home-url "http://threecrickets.com/prudence/")
(def application-contact-email "prudence@threecrickets.com")

;
; Debugging
;

; Set to true to show debug information on error.

(def show-debug-on-error false)

; The base URL for showing source code (only relevant when show-debug-on-error is true). 

(def show-source-code-url "/sourcecode/")

; This is so we can see the source code for scripts by adding ?source=true
; to the URL. You probably wouldn't want this for most applications.

(def source-viewable true)

;
; Performance
;

; This is the time (in milliseconds) allowed to pass until a script file
; is tested to see if it was changed. During development, you'd want this
; to be low, but during production, it should be high in order to avoid
; unnecessary hits on the filesystem.

(def minimum-time-between-validity-checks 1000)

;
; Documents
;

; If a document name points to a directory rather than a file, and that directory
; contains a file with this name, then it will be used. This allows
; you to use the directory structure to create nice URLs without relying
; on filenames.

(def documents-default-name "default")

; Documents will always be looked for here.

(def libraries-base-path "/libraries/")

;
; Logging
;
; Logger defaults to the application's directory name. Configure logging at
; conf/logging.conf.
;

;(def application-logger-name "prudence-application")

;
; Hosts
;
; This map matches the virtual hosts to which our application will be attached
; with their base URLs on the hosts. See component/hosts.py for more information.
; Specify None for the URL to default to the application's directory name.
;

(def hosts {(.getDefaultHost component) nil})

;
; Resources
;
; Sets up a directory under which you can place script files that implement
; RESTful resources. The directory structure underneath the base directory
; is directly linked to the base URL.
;

(def resources-base-url "/")
(def resources-base-path "/resources/")

; These documents are allowed to be under libraries-base-path as well as under
; resources-base-path.

(def resources-pass-through [])

; Set this to True if you want to start to load and compile your
; resources as soon as Prudence starts.

(def resources-defrost true)

;
; Dynamic Web
;
; Sets up a directory under which you can place text files that support embedded scriptlets.
; Note that the generated result can be cached for better performance.
;

(def dynamic-web-base-url "/")
(def dynamic-web-base-path "/web/dynamic/")
(def fragments-base-path "/web/fragments/")

; If the URL points to a directory rather than a file, and that directory
; contains a file with this name, then it will be used. This allows
; you to use the directory structure to create nice URLs that do not
; contain filenames.

(def dynamic-web-default-document "index")

; These documents are allowed to be under fragments-base-path as well as under
; dynamic-web-base-path.

(def dynamic-web-pass-through [])

; Set this to true if you want to compile your scriptlets as soon as Prudence
; starts.

(def dynamic-web-defrost true)

; Set this to true if you want to load all your dynamic web documents as soon
; as Prudence starts.

(def dynamic-web-preheat true)

; Client caching mode: 0=disabled, 1=conditional, 2=offline

(def dynamic-web-client-caching-mode 1)

;
; Static Web
;
; Sets up a directory under which you can place static files of any type.
; Servers like Grizzly and Jetty can use non-blocking I/O to stream static
; files efficiently to clients. 
;

(def static-web-base-url "/")
(def static-web-base-path "/web/static/")

; Whether to enable smart compression on HTTP representations.

(def static-web-compress true)

; If the URL points to a directory rather than a file, then this will allow
; automatic creation of an HTML page with a directory listing.

(def static-web-directory-listing-allowed true)

;
; File Uploads
;

; Temporary files for uploads will be stored in this subdirectory.

(def file-upload-base-path "/uploads/")

; The size in bytes beyond which uploaded files will be stored to disk.
; Defaults to zero, meaning that all uploaded files will be stored to disk.

(def file-upload-size-threshold 0)

;
; Handlers
;
; Sets up a directory under which you can place script files that implement
; general-purpose handlers.
;

(def handlers-base-path "/handlers/")

;
; Tasks
;
; Sets up a directory where you can place script files schedule to run
; according to the application's crontab file.
;

(def tasks-base-path "/tasks/")

;
; Preheater
;
; List resources here that you want heated up as soon as Prudence starts.
;

(def preheat-resources [])

;
; URL Manipulation
;

; The URLs in this array will automatically be redirected to have a trailing
; slash added to them if it's missing.

(def url-add-trailing-slash [dynamic-web-base-url static-web-base-url])

;
; Predfined Globals
;
; These will be available to your code via (.getGlobals application).
;

(def predefined-globals {})
