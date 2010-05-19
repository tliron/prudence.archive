;
; Prudence Guide Settings
;

(.. executable getContainer (execute "defaults/application/settings/"))

(def application-name "Prudence Test")
(def application-description "Prudence tests")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://threecrickets.com/prudence/")
(def application-contact-email "prudence@threecrickets.com")

(def hosts {(.getDefaultHost component) nil mysite-host nil})

(def show-debug-on-error true)

(def preheat-resources ["/data/jython/" "/data/jruby/" "/data/groovy/" "/data/clojure/" "/data/quercus/" "/data/rhino/"])
