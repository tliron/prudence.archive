;
; Prudence Routing
;
; Copyright 2009-2011 Three Crickets LLC.
;
; The contents of this file are subject to the terms of the LGPL version 3.0:
; http://www.gnu.org/copyleft/lesser.html
;
; Alternatively, you can obtain a royalty free commercial license with less
; limitations, transferable or non-transferable, directly from Three Crickets
; at http://threecrickets.com/
;

(import
  'java.io.File
  'java.util.concurrent.CopyOnWriteArrayList
  'com.threecrickets.prudence.util.IoUtil)

; Hosts

(execute-or-default "instance/hosts/")

; Unzip

(def common-dir (File. (.. document getSource getBasePath) "common"))
(def properties-file (File. common-dir "common.properties"))
(def properties (IoUtil/loadProperties properties-file))
(def save-properties false)
(def common-files (.listFiles common-dir))
(doseq [common-file (filter #(and (not (.isDirectory %)) (.. % getName (endsWith ".zip"))) common-files)]
  (let [last-modified (str (.lastModified common-file))]
    (when (not (.equals (.getProperty properties (.getName common-file) "") last-modified))
		  (println (str "Unpacking \"" (.getName common-file) "\"..."))
		  (IoUtil/unzip common-file common-dir)
		  (.setProperty properties (.getName common-file) last-modified)
		  (def save-properties true))))
(if save-properties
  (IoUtil/saveProperties properties properties-file))

(def applications-dir (File. (.. document getSource getBasePath) "applications"))
(def properties-file (File. applications-dir "applications.properties"))
(def properties (IoUtil/loadProperties properties-file))
(def save-properties false)
(def applications-files (.listFiles applications-dir))
(doseq [applications-file (filter #(and (not (.isDirectory %)) (.. % getName (endsWith ".zip"))) applications-files)]
  (let [last-modified (str (.lastModified applications-file))]
    (when (not (.equals (.getProperty properties (.getName applications-file) "") last-modified))
		  (println (str "Unpacking \"" (.getName applications-file) "\"..."))
		  (IoUtil/unzip applications-file applications-dir)
		  (.setProperty properties (.getName applications-file) last-modified)
		  (def save-properties true))))
(if save-properties
  (IoUtil/saveProperties properties properties-file))

; Applications

(def applications (CopyOnWriteArrayList.)) 
(.. component getContext getAttributes (put "com.threecrickets.prudence.applications" applications))

(def application-dirs (.listFiles applications-dir))
(doseq [application-dir (filter #(and (.isDirectory %) (not (.isHidden %))) application-dirs)]
	(def application-name (.getName application-dir))
  (def application-internal-name (.getName application-dir))
	(def application-logger-name (.getName application-dir))
	(def application-base-path (.getPath application-dir))
	(def application-default-url (str "/" (.getName application-dir)))
	(def application-instance nil) ; otherwise the below would create it in a different namespace
	(def application-base (str "applications/" (.getName application-dir)))
  (execute-or-default application-base "defaults/application/")
  (.add applications application-instance))

(if (empty? applications) (do
 		(print "No applications found. Exiting.\n")
  	(System/exit 0)))
