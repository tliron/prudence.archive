
(.. prudence (include "../libraries/stickstick/data/"))

(use
	'clojure.contrib.json.read
	'clojure.contrib.json.write
	'clojure.contrib.sql)

(import 'java.io.File)

(defn get-id []
	(try
  	(Integer/parseInt (.. prudence getResource getRequest getAttributes (get "id")))
  	(catch Exception _ nil)))

	;(let [form (.. prudence getResource getRequest getResourceRef getQueryAsForm)]
	;	(Integer/parseInt (.getFirstValue form "id")))

(defn handle-init []
	(.. prudence (addMediaTypeByName "text/plain"))
	(.. prudence (addMediaTypeByName "application/json")))

(defn handle-get []
	(let [id (get-id)]

		(with-connection from-pool
			(let [note (get-note id)]
				(if (nil? note)
					404
					(do
						(.setModificationTimestamp prudence (note :timestamp))
						(let [note (dissoc note :timestamp)]
							(json-str note))))))))

(defn handle-get-info []
	(let [id (get-id)]

		(with-connection from-pool
			(let [note (get-note id)]
				(if (nil? note)
					nil
					(note :timestamp))))))

(defn handle-post []
	(let [id (get-id)]

    ; Note: You can only "consume" the entity once, so if we want it
    ; as text, and want to refer to it more than once, we should keep
    ; a reference to that text.
    
    (let [text (.. prudence getEntity (getText))
    	note (read-json text)]

			(with-connection from-pool
				(let [existing (get-note id)]
					(if (nil? existing)
						404
						(let [note (merge existing note)]
							(update-note note)
							(update-board-timestamp note))))))))

(defn handle-delete []
	(let [id (get-id)]

		(with-connection from-pool
			(let [note (get-note id)]
				(if (nil? note)
					404
					(do
						(delete-note note)
						(update-board-timestamp note)
						nil))))))
