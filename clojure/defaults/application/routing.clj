;
; Prudence Application Routing
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
  'java.lang.ClassLoader
  'java.io.File
  'java.util.concurrent.ConcurrentHashMap
  'java.util.concurrent.CopyOnWriteArrayList
  'org.restlet.routing.Router
  'org.restlet.routing.Redirector
  'org.restlet.routing.Template
  'org.restlet.resource.Finder
  'org.restlet.resource.Directory
  'org.restlet.engine.application.Encoder
  'com.threecrickets.scripturian.util.DefrostTask
  'com.threecrickets.scripturian.document.DocumentFileSource
  'com.threecrickets.prudence.PrudenceRouter
  'com.threecrickets.prudence.util.Fallback
  'com.threecrickets.prudence.util.PreheatTask
  'com.threecrickets.prudence.util.PhpExecutionController)

(def classLoader (ClassLoader/getSystemClassLoader))

;
; Utilities
;

; Makes sure we have slashes where we expect them
(defn fix-url [url]
  (let [url (.replace url "//" "/")] ; no doubles
    (let [url (if (not (.startsWith url "/")) (str "/" url) url)] ; always at the beginning
      (if (and (not (.equals url "/")) (not (.endsWith url "/"))) ; always at the end
        (str url "/")
        url))))

;
; Internal router
;

(.setMatchingMode (.. component getInternalRouter (attach (str "/" application-internal-name) application-instance)) Template/MODE_STARTS_WITH)

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
      (let [url (if (.endsWith url "/") (.substring url 0 (- (.length url) 1)) url)] ; No trailing slash
        (print (str "\"" url "/\" on") (.getName host))
        (.setMatchingMode (.attach host url application-instance) Template/MODE_STARTS_WITH)
        (if-not (.equals url "")
          (.setMatchingMode (.attach host url add-trailing-slash) Template/MODE_EQUALS))))
    (if-not (empty? others)
      (do
        (print ", ")
        (recur others)))))

(print (str (.getName application-instance) ": "))
(add-to-hosts (.entrySet hosts))
(println ".")

(def application-globals (.. application-instance getContext (getAttributes)))

(.put application-globals "com.threecrickets.prudence.component" component)
(def cache (.. component getContext getAttributes (get "com.threecrickets.prudence.cache")))
(if (not (nil? cache))
  (.put application-globals "com.threecrickets.prudence.cache" cache))

;
; Inbound root
;

(def router (PrudenceRouter. (.getContext application-instance) minimum-time-between-validity-checks))
(.setRoutingMode router Router/MODE_BEST_MATCH)
(.setInboundRoot application-instance router)

;
; Add trailing slashes
;

(doseq [url url-add-trailing-slash]
  (let [url (fix-url url)]
    (if (> (.length url) 0)
      (let [url (if (.endsWith url "/") (.substring url 0 (- (.length url) 1)) url)]
        (.attach router url add-trailing-slash)))))

(def language-manager (.. executable getManager))

;
; Libraries
;

(def libraries-document-sources (CopyOnWriteArrayList.))
(.add libraries-document-sources (DocumentFileSource. (str application-base libraries-base-path) (str application-base-path libraries-base-path) documents-default-name "clj" (.longValue minimum-time-between-validity-checks)))
(.add libraries-document-sources common-libraries-document-source)

;
; Dynamic web
;

(def dynamic-web-document-source (DocumentFileSource. (str application-base dynamic-web-base-path) (str application-base-path dynamic-web-base-path) dynamic-web-default-document "clj" (.longValue minimum-time-between-validity-checks)))
(def fragments-document-sources (CopyOnWriteArrayList.))
(.add fragments-document-sources (DocumentFileSource. (str application-base fragments-base-path) (str application-base-path fragments-base-path) dynamic-web-default-document "clj" (.longValue minimum-time-between-validity-checks)))
(.add fragments-document-sources common-fragments-document-source)
(def cache-key-pattern-handlers (ConcurrentHashMap.))
(def scriptlet-plugins (ConcurrentHashMap.))
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.documentSource" dynamic-web-document-source)
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.extraDocumentSources" fragments-document-sources)
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName" dynamic-web-default-document)
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.executionController" (PhpExecutionController.)) ; Adds PHP predefined variables
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.clientCachingMode" dynamic-web-client-caching-mode)
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers" cache-key-pattern-handlers)
(.put application-globals "com.threecrickets.prudence.GeneratedTextResource.scriptletPlugins" scriptlet-plugins)

(def dynamic-web (Finder. (.getContext application-instance) (.loadClass classLoader "com.threecrickets.prudence.GeneratedTextResource")))
(def dynamic-web-base-url (fix-url dynamic-web-base-url))
(.attachBase router dynamic-web-base-url dynamic-web)

(if dynamic-web-defrost
  (doseq [defrost-task (DefrostTask/forDocumentSource dynamic-web-document-source language-manager "clojure" true true)]
    (def tasks (conj tasks defrost-task))))

;
; Static web
;

(def static-web (Fallback. (.getContext application-instance) minimum-time-between-validity-checks))
(def directory (Directory. (.getContext application-instance) (.. (File. (str application-base-path static-web-base-path)) toURI (toString))))
(.setListingAllowed directory static-web-directory-listing-allowed)
(.setNegotiatingContent directory true)
(.addTarget static-web directory)
(def directory (Directory. (.getContext application-instance) (.. (File. (str (.. document getSource (getBasePath)) "common/web/static/")) toURI (toString))))
(.setListingAllowed directory static-web-directory-listing-allowed)
(.setNegotiatingContent directory true)
(.addTarget static-web directory)

(def static-web-base-url (fix-url static-web-base-url))
(if static-web-compress
  (let [encoder (Encoder. (.getContext application-instance))]
    (.setNext encoder static-web)
    (def static-web encoder)))
(.attachBase router static-web-base-url static-web)

;
; Resources
;

(def resources-document-source (DocumentFileSource. (str application-base resources-base-path) (str application-base-path resources-base-path) documents-default-name "clj" (.longValue minimum-time-between-validity-checks))) 
(.put application-globals "com.threecrickets.prudence.DelegatedResource.documentSource" resources-document-source)

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
    (.put application-globals "com.threecrickets.prudence.SourceCodeResource.documentSources" [dynamic-web-document-source resources-document-source])
    
    (def source-code (Finder. (.getContext application-instance) (.loadClass classLoader "com.threecrickets.prudence.SourceCodeResource")))
    (def show-source-code-url (fix-url show-source-code-url))
    (.setMatchingMode (.attach router show-source-code-url source-code) Template/MODE_EQUALS)))

;
; Preheat
;

(if dynamic-web-preheat
  (doseq [preheat-task (PreheatTask/forDocumentSource dynamic-web-document-source application-internal-name application-instance application-logger-name)]
    (def tasks (conj tasks preheat-task))))

(doseq [preheat-resource preheat-resources]
  (def tasks (conj tasks (PreheatTask. application-internal-name preheat-resource application-instance application-logger-name))))
