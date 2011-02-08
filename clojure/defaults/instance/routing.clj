;
; Prudence Routing
;
; Copyright 2009-2011 Three Crickets LLC.
;
; The contents of this file are subject to the terms of the LGPL version 3.0:
; http://www.opensource.org/licenses/lgpl-3.0.html
;
; Alternatively, you can obtain a royalty free commercial license with less
; limitations, transferable or non-transferable, directly from Three Crickets
; at http://threecrickets.com/
;

(import
  'java.io.File
  'com.threecrickets.prudence.util.IoUtil)

; Hosts

(execute-or-default "instance/hosts/")

; Applications

(def applications []) 
(def applications-dir (File. (.. document getSource getBasePath) "applications"))

(def properties-file (File. applications-dir "applications.properties"))
(def properties (IoUtil/loadProperties properties-file))
(def save-properties false)
(def application-files (.listFiles applications-dir))
(doseq [application-file (filter #(and (not (.isDirectory %)) (.. % getName (endsWith ".zip"))) application-files)]
  (let [last-modified (str (.lastModified application-file))]
    (when (not (.equals (.getProperty properties (.getName application-file) "") last-modified))
		  (println (str "Unpacking \"" (.getName application-file) "\"..."))
		  (IoUtil/unzip application-file applications-dir)
		  (.setProperty properties (.getName application-file) last-modified)
		  (def save-properties true))))
(if save-properties
  (IoUtil/saveProperties properties properties-file))

(def application-dirs (.listFiles applications-dir))
(doseq [application-dir (filter #(and (.isDirectory %) (not (.isHidden %))) application-dirs)]
	(def application-name (.getName application-dir))
  (def application-internal-name (.getName application-dir))
	(def application-logger-name (.getName application-dir))
	(def application-base-path (.getPath application-dir))
	(def application-default-url (str "/" (.getName application-dir) "/"))
	(def application-instance nil) ; otherwise the below would create it in a different namespace
	(def application-base (str "applications/" (.getName application-dir) "/"))
  (execute-or-default application-base "defaults/application/")
  (def applications (conj applications application-instance)))

(.. component getContext getAttributes (put "com.threecrickets.prudence.applications" applications))

(if (empty? applications) (do
 		(print "No applications found. Exiting.\n")
  	(System/exit 0)))
