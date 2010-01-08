;
; Prudence Admin Settings
;

(.. document getContainer (include "defaults/application/settings"))

(def application-name "Prudence Admin")
(def application-description "Runtime management of Prudence")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://www.threecrickets.com/prudence/")
(def application-contact-email "prudence@threecrickets.com")

(def hosts {(.getDefaultHost component) "/" mysite-host "/"})
