;
; Prudence Admin Settings
;

(.. executable getContainer (include "defaults/application/settings/"))

(def application-name "Stickstick")
(def application-description "Share online sticky notes")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://threecrickets.com/prudence/stickstick/")
(def application-contact-email "prudence@threecrickets.com")

(def runtime-attributes (merge runtime-attributes
	{"stickstick.backend" "h2"}
	{"stickstick.username" "root"}
	{"stickstick.password" "root"}
	{"stickstick.host" ""}
	{"stickstick.database" "h2/stickstick"}))

(def show-debug-on-error true)

(def preheat-resources ["data/"])
