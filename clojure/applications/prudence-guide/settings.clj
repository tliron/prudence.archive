;
; Prudence Guide Settings
;

(.. executable getContainer (execute "defaults/application/settings/"))

(def application-name "Prudence Guide")
(def application-description "Prudence web site, documentation, and tests")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://threecrickets.com/prudence/")
(def application-contact-email "prudence@threecrickets.com")

(def hosts {(.getDefaultHost component) nil mysite-host nil})

(def show-debug-on-error true)

(def preheat-resources ["/data/jython/" "/data/jruby/" "/data/groovy/" "/data/clojure/" "/data/rhino/"])
