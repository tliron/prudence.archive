
(.. prudence (include "../libraries/stickstick/data/"))

(import 'java.io.File)

(add-classpath (.toURL (File. (str (.. prudence getSource getBasePath) "/../libraries/clojure"))))
(use '[org.danlarkin.json :only (encode-to-str decode-from-str)])

(defn get-id []
	(try
  	(Integer/parseInt (.. prudence getResource getRequest getAttributes (get "id")))
  	(catch Exception _ nil))

	;(let [form (.. prudence getResource getRequest getResourceRef getQueryAsForm)]
	;	(Integer/parseInt (.getFirstValue form "id")))
)

(defn handle-init []
	(.. prudence (addMediaTypeByName "text/plain"))
	(.. prudence (addMediaTypeByName "application/json")))

(defn handle-get []
	(let [id (get-id)]

		(let [connection (get-connection)]
			(try
				(let [note (get-note id connection)]
					(if (nil? note)
						404
						(do
							(.setModificationTimestamp prudence (note :timestamp))
							(let [note (dissoc note :timestamp)]
								(encode-to-str note)))))
		    (finally
		    	(if (not (nil? connection))
		    		(.close connection)))))))

(defn handle-get-info []
	(let [id (get-id)]

		(let [connection (get-connection)]
			(try
				(let [note (get-note id connection)]
					(if (nil? note)
						nil
						(note :timestamp)))
		    (finally
		    	(if (not (nil? connection))
		    		(.close connection)))))))

(defn handle-post []
	(let [id (get-id)]

    ; Note: You can only "consume" the entity once, so if we want it
    ; as text, and want to refer to it more than once, we should keep
    ; a reference to that text.
    
    (let [text (.. prudence getEntity (getText))
    	note (decode-from-str text)]

			(let [connection (get-connection)]
				(try
					(let [existing (get-note id connection)]
						(if (nil? existing)
							404
							(let [note (merge existing note)]
								(update-note note connection)
								(update-board-timestamp note connection))))
			    (finally
			    	(if (not (nil? connection))
			    		(.close connection))))))))

(defn handle-delete []
	(let [id (get-id)]

		(let [connection (get-connection)]
			(try
				(let [note (get-note id connection)]
					(if (nil? note)
						404
						(do
							(delete-note note connection)
							(update-board-timestamp note connection)
							nil)))
		    (finally
		    	(if (not (nil? connection))
		    		(.close connection)))))))