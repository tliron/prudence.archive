
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
