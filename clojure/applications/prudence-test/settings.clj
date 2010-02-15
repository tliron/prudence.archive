;
; Prudence Admin Settings
;

(.. document getContainer (include "defaults/application/settings"))

(def application-name "Prudence Test")
(def application-description "Used to test that Prudence works for you, and useful as a skeleton for creating your own applications")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://threecrickets.com/prudence/")
(def application-contact-email "prudence@threecrickets.com")

(def hosts {(.getDefaultHost component) nil mysite-host nil})

(def show-debug-on-error true)

(def preheat-resources ["/data/jython/" "/data/jruby/" "/data/groovy/" "/data/clojure/" "/data/rhino/"])
