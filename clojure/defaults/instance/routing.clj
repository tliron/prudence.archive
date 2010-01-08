;
; Prudence Routing
;

(import 'java.io.File)

; Hosts

(include-or-default "instance/hosts")

; Applications

(def applications []) 
(.. component getContext getAttributes (put "applications" applications))

(def application-dirs (.. (File. "applications") listFiles))

(doseq [application-dir (filter #(.isDirectory %) application-dirs)]
	(def application-name (.getName application-dir))
  (def application-internal-name (.getName application-dir))
	(def application-logger-name (.getName application-dir))
	(def application-base-path (.getPath application-dir))
	(def application-default-url (str "/" (.getName application-dir) "/"))
  (include-or-default application-base-path "defaults/application")
  (def applications (conj applications (resolve 'user/application))))

(if (empty? applications)
	(do
 		(print "No applications found. Exiting.")
  	(System/exit 0)))
