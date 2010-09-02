;
; Prudence Application Routing
;

(import
	'java.lang.ClassLoader
	'java.io.File
	'org.restlet.routing.Router
	'org.restlet.routing.Redirector
	'org.restlet.routing.Template
	'org.restlet.resource.Finder
	'org.restlet.resource.Directory
	'com.threecrickets.scripturian.util.DefrostTask
	'com.threecrickets.scripturian.document.DocumentFileSource
	'com.threecrickets.prudence.PrudenceRouter
	'com.threecrickets.prudence.util.PreheatTask
	'com.threecrickets.prudence.util.PhpExecutionController)

(def classLoader (ClassLoader/getSystemClassLoader))

;
; Utilities
;

; Makes sure we have slashes where we expect them
(defn fix-url [url]
	(let [url (.replace url "//" "/")] ; no doubles
		(let [url (if (.startsWith url "/") (.substring url 1) url)] ; never at the beginning
			(if (and (> (.length url) 0) (not (.endsWith url "/"))) ; always at the end
				(str url "/")
				url))))

;
; Internal router
;

(.setMatchingMode (.. component getInternalRouter (attach (str "/" application-internal-name "/") application-instance)) Template/MODE_STARTS_WITH)

;
; Hosts
;
; Note that the application's context will not be created until we attach the application-instance to at least one
; virtual host. See defaults/instance/hosts.clj for more information.
;

(def add-trailing-slash (Redirector. (.getContext application-instance) "{ri}/" Redirector/MODE_CLIENT_PERMANENT))

(defn add-to-hosts [entries]
	(let [[entry & others] (seq entries)
		host (.getKey entry)
		url (.getValue entry)]
		
		(let [url (if (nil? url) application-default-url url)]
			(print (str "\"" url "\"") "on" (.getName host))
			(.setMatchingMode (.attach host url application-instance) Template/MODE_STARTS_WITH)
			(if-not (= url "/")
				(let [url (if (.endsWith url "/") (.substring url 0 (- (.length url) 1)) url)]
					(.setMatchingMode (.attach host url add-trailing-slash) Template/MODE_EQUALS))))
		(if-not (empty? others)
			(do
				(print ", ")
				(recur others)))))

(print (str (.getName application-instance) ": "))
(add-to-hosts (.entrySet hosts))
(println ".")

(def attributes (.. application-instance getContext (getAttributes)))

(.put attributes "com.threecrickets.prudence.component" component)
(def cache (.. component getContext getAttributes (get "com.threecrickets.prudence.cache")))
(if (not (nil? cache))
	(.put attributes "com.threecrickets.prudence.cache" cache))

;
; Inbound root
;

(def router (PrudenceRouter. (.getContext application-instance)))
(.setRoutingMode router Router/MODE_BEST_MATCH)
(.setInboundRoot application-instance router)

;
; Add trailing slashes
;

(doseq [url url-add-trailing-slash]
	(let [url (fix-url url)]
		(if (> (.length url) 0)
			(let [url (if (.endsWith url "/") (.substring url (- (.length url) 1)) url)]
				(.attach router url add-trailing-slash)))))

(def language-manager (.. executable getManager))

;
; Handlers
;

(def handlers-document-source = (DocumentFileSource. (str application-base-path handlers-base-path) handlers-default-name "clj" (.longValue handlers-minimum-time-between-validity-checks)))
(.setFilterDocumentSource router handlers-document-source)
(.setFilterLanguageManager router language-manager)

;
; Dynamic web
;

(def dynamic-web-document-source (DocumentFileSource. (str application-base-path dynamic-web-base-path) dynamic-web-default-document "clj" (.longValue dynamic-web-minimum-time-between-validity-checks)))
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.languageManager" language-manager)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag" "clojure")
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultName" dynamic-web-default-document)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.documentSource" dynamic-web-document-source)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.sourceViewable" dynamic-web-source-viewable)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.executionController" (PhpExecutionController.)) ; Adds PHP predefined variables
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.clientCachingMode" dynamic-web-client-caching-mode)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.fileUploadSizeThreshold" file-upload-size-threshold)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.handlersDocumentSource" handlers-document-source)

(def dynamic-web (Finder. (.getContext application-instance) (.loadClass classLoader "com.threecrickets.prudence.GeneratedTextResource")))
(def dynamic-web-base-url (fix-url dynamic-web-base-url))
(.attachBase router dynamic-web-base-url dynamic-web)

(if dynamic-web-defrost
	(doseq [defrost-task (DefrostTask/forDocumentSource dynamic-web-document-source language-manager "clojure" true true)]
		(def tasks (conj tasks defrost-task))))

;
; Static web
;

(def static-web (Directory. (.getContext application-instance) (.. (File. (str application-base-path static-web-base-path)) toURI (toString))))
(.setListingAllowed static-web static-web-directory-listing-allowed)
(.setNegotiateContent static-web true)
(def static-web-base-url (fix-url static-web-base-url))
(.attachBase router static-web-base-url static-web)

;
; Resources
;

(def resources-document-source (DocumentFileSource. (str application-base-path resources-base-path) resources-default-name "clj" (.longValue resources-minimum-time-between-validity-checks))) 
(.put attributes "com.threecrickets.prudence.DelegatedResource.languageManager" language-manager)
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultLanguageTag" "clojure")
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultName" resources-default-name)
(.put attributes "com.threecrickets.prudence.DelegatedResource.documentSource" resources-document-source)
(.put attributes "com.threecrickets.prudence.DelegatedResource.sourceViewable" resources-source-viewable)
(.put attributes "com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold" file-upload-size-threshold)

(def resources (Finder. (.getContext application-instance) (.loadClass classLoader "com.threecrickets.prudence.DelegatedResource")))
(def resources-base-url (fix-url resources-base-url))
(.attachBase router resources-base-url resources)

(if resources-defrost
	(doseq [defrost-task (DefrostTask/forDocumentSource resources-document-source language-manager "clojure" false true)]
		(def tasks (conj tasks defrost-task))))

;
; SourceCode
;

(if show-debug-on-error
	(do
		(.put attributes "com.threecrickets.prudence.SourceCodeResource.documentSources" [dynamic-web-document-source resources-document-source])
		
		(def source-code (Finder. (.getContext application-instance) (.loadClass classLoader "com.threecrickets.prudence.SourceCodeResource")))
    (def show-source-code-url (fix-url show-source-code-url))
		(.setMatchingMode (.attach router show-source-code-url source-code) Template/MODE_EQUALS)))

;
; Preheat
;

(if dynamic-web-preheat
	(doseq [preheat-task (PreheatTask/forDocumentSource dynamic-web-document-source (.getContext component) application-internal-name)]
		(def tasks (conj tasks preheat-task))))

(doseq [preheat-resource preheat-resources]
	(def tasks (conj tasks (PreheatTask. (.getContext component) application-internal-name preheat-resource))))
