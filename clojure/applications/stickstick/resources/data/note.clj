
(.. prudence (include "../libraries/stickstick/data/"))

(import
	'java.io.File)

(add-classpath (.toURL (File. (str (.. prudence getSource getBasePath) "/../libraries/clojure"))))
(use '[org.danlarkin.json :only (encode-to-str decode-from-str)])

(defn get-id []
	(try
  	(Integer/parseInt (.. prudence getResource getRequest getAttributes (get "id")))
  	(catch Exception x nil))

	;(let [form (.. prudence getResource getRequest getResourceRef getQueryAsForm)]
	;	(Integer/parseInt (.getFirstValue form "id")))
)

(defn handle-init []
	(.. prudence (addMediaTypeByName "text/plain"))
	(.. prudence (addMediaTypeByName "application/json")))

(defn handle-get []
	(let [id (get-id)]
		(str id)))