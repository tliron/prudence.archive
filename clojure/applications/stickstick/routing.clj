;
; Stickstick Routing
;

(.execute document "defaults/application/routing/")

(import
	'com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter
	'com.threecrickets.prudence.util.CssUnifyMinifyFilter)

(.capture router (fix-url (str resources-base-url "/data/note/{id}/")) "/data/note/")

; Wrap the static web with unify-minify filters
(.detach router static-web)
(def wrapped-static-web (CssUnifyMinifyFilter. (.getContext application-instance) static-web (File. (str application-base-path static-web-base-path)) dynamic-web-minimum-time-between-validity-checks))
(def wrapped-static-web (JavaScriptUnifyMinifyFilter. (.getContext application-instance) wrapped-static-web (File. (str application-base-path static-web-base-path)) dynamic-web-minimum-time-between-validity-checks))
(.attachBase router (fix-url static-web-base-url) wrapped-static-web)
