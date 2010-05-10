;
; Stickstick Routing
;

(.. executable getContainer (execute "defaults/application/routing/"))

(import 'com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter)

(.capture router (fix-url (str resources-base-url "/data/note/{id}/")) "/data/note/")

; Wrap the static web with a JavaScriptUnifyMinifyFilter
(.detach router static-web)
(.setMatchingMode
	(.attach router
		(fix-url static-web-base-url)
		(JavaScriptUnifyMinifyFilter. (.getContext application-instance) static-web (File. (str application-base-path static-web-base-path)) dynamic-web-minimum-time-between-validity-checks))
	Template/MODE_STARTS_WITH)
