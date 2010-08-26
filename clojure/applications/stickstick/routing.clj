;
; Stickstick Routing
;

(.execute document "defaults/application/routing/")

(import
	'com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter
	'com.threecrickets.prudence.util.CssUnifyMinifyFilter)

(.capture router (str resources-base-url "data/note/{id}/") "data/note/")
(.hide router "data/note/")

; Wrap the static web with unify-minify filters
(def css-filter (CssUnifyMinifyFilter. nil (File. (str application-base-path static-web-base-path)) dynamic-web-minimum-time-between-validity-checks))
(def java-script-filter (JavaScriptUnifyMinifyFilter. nil (File. (str application-base-path static-web-base-path)) dynamic-web-minimum-time-between-validity-checks))
(.filterBase router static-web-base-url css-filter static-web)
(.filterBase router static-web-base-url java-script-filter css-filter)
