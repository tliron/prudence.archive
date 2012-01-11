
(ns prudence.dispatch.resource)

(defprotocol Resource
  (handle-init [resource conversation])
  (handle-get [resource conversation])
  (handle-get-info [resource conversation])
  (handle-post [resource conversation])
  (handle-put [resource conversation])
  (handle-delete [resource conversation]))
