;
; Prudence Application Routing
;

(import
	'java.lang.ClassLoader
	'java.io.File
	'javax.script.ScriptEngineManager
	'org.restlet.routing.Router
	'org.restlet.routing.Redirector
	'org.restlet.routing.Template
	'org.restlet.resource.Finder
	'org.restlet.resource.Directory
	'com.threecrickets.scripturian.Defroster
	'com.threecrickets.scripturian.file.DocumentFileSource
	'com.threecrickets.prudence.util.FallbackRouter
	'com.threecrickets.prudence.util.PreheatTask)

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
; Internal Router
;

(.setMatchingMode (.. component getInternalRouter (attach (str "/" application-internal-name) application)) Template/MODE_STARTS_WITH)

;
; Hosts
;
; Note that the application's context will not be created until we attach the application to at least one
; virtual host. See component/hosts.clj for more information.
;

(def add-trailing-slash (Redirector. (.getContext application) "{ri}/" Redirector/MODE_CLIENT_PERMANENT))

(defn add-to-hosts [entries]
	(let [entry (first entries) rest (rest entries) host (.getKey entry) url (.getValue entry)]
		(let [url (if (nil? url) application-default-url url)]
			(print (str "\"" url "\"") "on" (.getName host))
			(.setMatchingMode (.attach host url application) Template/MODE_STARTS_WITH)
			(if (not= url "/")
				(let [url (if (.endsWith url "/") (.substring url 0 (- (.length url) 1)) url)]
					(.attach host url add-trailing-slash))))
		(if (not (empty? rest)) (do
			(print ", ")
			(recur rest)))))

(print (str (.getName application) ": "))
(add-to-hosts (.entrySet hosts))
(println ".")

(def attributes (.. application getContext (getAttributes)))

;
; Inbound root
;

(def router (FallbackRouter. (.getContext application)))
(.setRoutingMode router Router/MODE_BEST_MATCH)
(.setInboundRoot application router)

;
; Add trailing slashes
;

(doseq [url url-add-trailing-slash]
	(let [url (fix-url url)]
		(if (> (.length url) 0)
			(let [url (if (.endsWith url "/") (.substring url (- (.length url) 1)) url)]
				(.attach router url add-trailing-slash)))))

;
; Dynamic web
;

(def script-engine-manager (ScriptEngineManager.))
(def dynamic-web-document-source (DocumentFileSource. (str application-base-path dynamic-web-base-path) dynamic-web-default-document (.longValue dynamic-web-minimum-time-between-validity-checks)))
(if dynamic-web-defrost
	(.defrost (Defroster. script-engine-manager document-source true) executor))
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.engineManager" script-engine-manager)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultEngineName" "Clojure")
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultName" dynamic-web-default-document)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.documentSource" dynamic-web-document-source)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.sourceViewable" dynamic-web-source-viewable)

(def dynamic-web (Finder. (.getContext application) (.loadClass classLoader "com.threecrickets.prudence.GeneratedTextResource")))
(.setMatchingMode (.attach router (fix-url dynamic-web-base-url) dynamic-web) Template/MODE_STARTS_WITH)

;
; Static web
;

(def static-web (Directory. (.getContext application) (.. (File. (str application-base-path static-web-base-path)) toURI (toString))))
(.setListingAllowed static-web static-web-directory-listing-allowed)
(.setNegotiateContent static-web true)
(.setMatchingMode (.attach router (fix-url static-web-base-url) static-web) Template/MODE_STARTS_WITH)

;
; Resources
;

(def resources-document-source (DocumentFileSource. (str application-base-path resources-base-path) resources-default-name (.longValue resources-minimum-time-between-validity-checks))) 
(if resources-defrost
	(.defrost (Defroster. script-engine-manager document-source true) executor))
(.put attributes "com.threecrickets.prudence.DelegatedResource.engineManager" script-engine-manager)
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultEngineName" "Clojure")
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultName" resources-default-name)
(.put attributes "com.threecrickets.prudence.DelegatedResource.documentSource" resources-document-source)
(.put attributes "com.threecrickets.prudence.DelegatedResource.sourceViewable" resources-source-viewable)

(def resources (Finder. (.getContext application) (.loadClass classLoader "com.threecrickets.prudence.DelegatedResource")))
(.setMatchingMode (.attach router (fix-url resources-base-url) resources) Template/MODE_STARTS_WITH)

;
; Preheat
;

(if dynamic-web-preheat
	(doseq [preheat-task (PreheatTask/create (.getContext component) application-internal-name dynamic-web-document-source)]
		(def tasks (conj tasks preheat-task))))

(doseq [preheat-resource preheat-resources]
	(def tasks (conj tasks (PreheatTask. (.getContext component) application-internal-name preheat-resource))))
