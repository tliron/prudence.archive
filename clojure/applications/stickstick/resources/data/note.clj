
(.. prudence (execute "../libraries/stickstick/data/"))

(use
	'clojure.contrib.json.read
	'clojure.contrib.json.write
	'clojure.contrib.sql)

(import 'java.io.File)

(defn get-id [conversation]
	(try
  	(Integer/parseInt (.. conversation getResource getRequest getAttributes (get "id")))
  	(catch Exception _ nil)))

	;(let [form (.. conversation getResource getRequest getResourceRef getQueryAsForm)]
	;	(Integer/parseInt (.getFirstValue form "id")))

(defn handle-init [conversation]
	(.. conversation (addMediaTypeByName "text/plain"))
	(.. conversation (addMediaTypeByName "application/json")))

(defn handle-get [conversation]
	(let [id (get-id conversation)]

		(with-connection from-pool
			(let [note (get-note id)]
				(if (nil? note)
					404
					(do
						(.setModificationTimestamp conversation (note :timestamp))
						(let [note (dissoc note :timestamp)]
							(json-str note))))))))

(defn handle-get-info [conversation]
	(let [id (get-id conversation)]

		(with-connection from-pool
			(let [note (get-note id)]
				(if (nil? note)
					nil
					(note :timestamp))))))

(defn handle-post [conversation]
	(let [id (get-id conversation)]

    ; Note: You can only "consume" the entity once, so if we want it
    ; as text, and want to refer to it more than once, we should keep
    ; a reference to that text.
    
    (let [text (.. conversation getEntity (getText))
    	note (keyword-map (read-json text))]

			;(println note)
			(with-connection from-pool
				(let [existing (get-note id)]
					(if (nil? existing)
						404
						(let [note (merge existing note)]
							;(println note)
							(let [note (update-note note)]
								;(println note)
								(update-board-timestamp note)
								(.setModificationTimestamp conversation (note :timestamp))
								(let [note (dissoc note :timestamp)]
									(json-str note))))))))))

(defn handle-delete [conversation]
	(let [id (get-id conversation)]

		(with-connection from-pool
			(let [note (get-note id)]
				(if (nil? note)
					404
					(do
						(delete-note note)
						(update-board-timestamp note (System/currentTimeMillis))
						nil))))))
