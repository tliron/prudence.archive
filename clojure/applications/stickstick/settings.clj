;
; Prudence Admin Settings
;

(.execute document "/defaults/application/settings/")

(def application-name "Stickstick")
(def application-description "Share online sticky notes")
(def application-author "Tal Liron")
(def application-owner "Three Crickets")
(def application-home-url "http://threecrickets.com/prudence/stickstick/")
(def application-contact-email "prudence@threecrickets.com")

(def predefined-globals (merge predefined-globals
	{"stickstick.backend" "h2"}
	{"stickstick.username" "root"}
	{"stickstick.password" "root"}
	{"stickstick.host" ""}
	{"stickstick.database" (str application-base-path "/data/stickstick")}))

(def show-debug-on-error true)

(def preheat-resources ["data/"])
