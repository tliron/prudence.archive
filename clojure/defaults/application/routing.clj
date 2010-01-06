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
	'com.threecrickets.prudence.util.PreheatTask
)

(def classLoader (ClassLoader/getSystemClassLoader))

;
; Utilities
;

; Makes sure we have slashes where we expect them
(defn fix-url [url]
	(let [url (.replace url "//" "/")]
		(if (and (> (.length url) 0) (= (.charAt url 0) "/"))
			(def url (.substring url 1))
		)
		(if (and (> (.length url) 0) (not= (.charAt url (- (.length url) 1)) "/"))
			(def url (str url "/"))
		)
		url
	)
)

;
; Internal Router
;

(.. component getInternalRouter (attach (str "/" application-internal-name application)))

;
; Hosts
;
; Note that the application's context will not be created until we attach the application to at least one
; virtual host. See component/hosts.js for more information.
;

(def add-trailing-slash (Redirector. (.getContext application) "{ri}/" Redirector/MODE_CLIENT_PERMANENT))

(print (str (.getName application) ": "))
(doseq [entry (.entrySet hosts)]
	(let [host (.getKey entry) url (.getValue entry)]
		(let [url (if (nil? url) application-default-url url)]
			(print (str "\"" url "\"") "on" (.getName host))
			(.setMatchingMode (.attach host url application) Template/MODE_STARTS_WITH)
			(if (not= url "/")
				(do
					(if (= (.charAt url (- (.length url) 1)) "/")
						(def url (.substring url (- (.length url) 1)))
					)
				)
				(.attach host url add-trailing-slash)
			)
		)
	)
	;(print ", ")
)
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

;if len(url-add-trailing-slash) > 0:
	;for url in url-add-trailing-slash:
		;url = fix-url(url)
		;if len(url) > 0:
			;if url[-1] == '/':
				;url = url[:-1]
			;router.attach(url, add-trailing-slash)

;
; Dynamic web
;

(def script-engine-manager (ScriptEngineManager.))
(def document-source (DocumentFileSource. (str application-base-path dynamic-web-base-path) dynamic-web-default-document (.longValue dynamic-web-minimum-time-between-validity-checks)))
(if dynamic-web-defrost
	(.defrost (Defroster. script-engine-manager document-source true) executor)
)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.engineManager" script-engine-manager)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultEngineName" "Clojure")
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.defaultName" dynamic-web-default-document)
(.put attributes "com.threecrickets.prudence.GeneratedTextResource.documentSource" document-source)
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

(def document-source (DocumentFileSource. (str application-base-path resource-base-path) resource-default-name (.longValue resource-minimum-time-between-validity-checks))) 
(if resource-defrost
	(.defrost (Defroster. script-engine-manager document-source true) executor)
)
(.put attributes "com.threecrickets.prudence.DelegatedResource.engineManager" script-engine-manager)
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultEngineName" "Clojure")
(.put attributes "com.threecrickets.prudence.DelegatedResource.defaultName" resource-default-name)
(.put attributes "com.threecrickets.prudence.DelegatedResource.documentSource" document-source)
(.put attributes "com.threecrickets.prudence.DelegatedResource.sourceViewable" resource-source-viewable)

(def resources (Finder. (.getContext application) (.loadClass classLoader "com.threecrickets.prudence.DelegatedResource")))
(.setMatchingMode (.attach router (fix-url resource-base-url) resources) Template/MODE_STARTS_WITH)

; Preheat resources

(doseq [preheat-resource preheat-resources]
	(def tasks (conj tasks (PreheatTask. (.getContext component) application-internal-name, preheat-resource)))
)
