;
; Prudence Admin Settings
;

(.. document getContainer (include "defaults/application/settings"))

(def application-name "Stickstick")
(def application-description "Share online sticky notes")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://www.threecrickets.com/prudence/stickstick/")
(def application-contact-email "prudence@threecrickets.com")

(def runtime-attributes (merge runtime-attributes
	{"stickstick.backend" "mysql+zxjdbc"}
	{"stickstick.username" "root"}
	{"stickstick.password" "root"}
	{"stickstick.host" "localhost"}
	{"stickstick.database" "stickstick"}
))

(def show-debug-on-error true)

(def preheat-resources (conj preheat-resources "notes/"))
