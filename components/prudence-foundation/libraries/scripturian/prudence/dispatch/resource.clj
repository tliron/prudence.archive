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

(ns prudence.dispatch.resource)

(defprotocol Resource
  (handle-init [resource conversation])
  (handle-get [resource conversation])
  (handle-get-info [resource conversation])
  (handle-post [resource conversation])
  (handle-put [resource conversation])
  (handle-delete [resource conversation]))
