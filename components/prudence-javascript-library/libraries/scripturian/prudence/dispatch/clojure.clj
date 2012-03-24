;
; This file is part of the Prudence Foundation Library
;
; Copyright 2011-2012 Three Crickets LLC.
;
; The contents of this file are subject to the terms of the LGPL version 3.0:
; http://www.gnu.org/copyleft/lesser.html
;
; Alternatively, you can obtain a royalty free commercial license with less
; limitations, transferable or non-transferable, directly from Three Crickets
; at http://threecrickets.com/
;

(require 'prudence.dispatch.resource)

(def resources {})

(.executeOnce document (.. application (getGlobals) (get "prudence.dispatch.clojure.library")))

(defn get-resource [conversation]
  (let
    [id (.. conversation (getLocals) (get "prudence.id"))]
    (resources id)))

(defn handle-init [conversation]
  (let
    [resource (get-resource conversation)]
    (if (nil? resource)
      404
	    (try
	      (prudence.dispatch.resource/handle-init resource conversation)
	      (catch AbstractMethodError _ 405)))))

(defn handle-get [conversation]
  (let
    [resource (get-resource conversation)]
    (if (nil? resource)
      404
	    (try
	      (prudence.dispatch.resource/handle-get resource conversation)
	      (catch AbstractMethodError _ 405)))))

(defn handle-get-info [conversation]
  (let
    [resource (get-resource conversation)]
    (if (nil? resource)
      404
	    (try
	      (prudence.dispatch.resource/handle-get-info resource conversation)
	      (catch AbstractMethodError _ 405)))))

(defn handle-post [conversation]
  (let
    [resource (get-resource conversation)]
    (if (nil? resource)
      404
	    (try
	      (prudence.dispatch.resource/handle-post resource conversation)
	      (catch AbstractMethodError _ 405)))))

(defn handle-put [conversation]
  (let
    [resource (get-resource conversation)]
    (if (nil? resource)
      404
	    (try
	      (prudence.dispatch.resource/handle-put resource conversation)
	      (catch AbstractMethodError _ 405)))))

(defn handle-delete [conversation]
  (let
    [resource (get-resource conversation)]
    (if (nil? resource)
      404
	    (try
	      (prudence.dispatch.resource/handle-delete resource conversation)
	      (catch AbstractMethodError x
	         (.. conversation (setStatusCode 405)))))))
