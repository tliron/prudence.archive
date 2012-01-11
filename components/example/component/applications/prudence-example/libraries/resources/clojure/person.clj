
(ns resources.clojure.person
  (:use prudence.dispatch.resource))

(deftype Person []
  Resource
	  (handle-init [resource conversation]
	     (.. conversation (addMediaTypeByName "text/html"))
	     (.. conversation (addMediaTypeByName "text/plain")))
	  (handle-get [resource conversation]
       (let
         [id (.. conversation getLocals (get "id"))]
         (str "I am person " id " formatted as \"" (.getMediaTypeName conversation) "\" encased in Clojure"))))
