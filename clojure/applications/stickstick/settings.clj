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

;(def runtime-attributes (cons runtime-attributes {"stickstick.backend" "mysql+zxjdbc"}))
;(def runtime-attributes (cons runtime-attributes {"stickstick.username" "root"}))
;(def runtime-attributes (cons runtime-attributes {"stickstick.password" "root"}))
;(def runtime-attributes (cons runtime-attributes {"stickstick.host" "localhost"}))
;(def runtime-attributes (cons runtime-attributes {"stickstick.database" "stickstick"}))

(def show-debug-on-error true)
