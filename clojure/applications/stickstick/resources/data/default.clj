
(.. prudence (include "../libraries/stickstick/data/"))

(use
	'clojure.contrib.json.read
	'clojure.contrib.json.write
	'clojure.contrib.sql)

(import 'java.io.File)

(defn handle-init [resource]
	(.. resource (addMediaTypeByName "text/plain"))
	(.. resource (addMediaTypeByName "application/json")))

; Returns [ids, max-timestamp]
(defn get-board-list [boards]
	(let [[board & others] boards]
		(if (empty? others)
			(if (nil? board)
				[[] nil]
				[[(board :id)] (board :timestamp)])
			(let [[other-ids others-max-timestamp] (get-board-list others)]
				[(cons (board :id) other-ids)
					(if (nil? (board :timestamp))
				 		nil
				 		(max (board :timestamp) others-max-timestamp))]))))

(defn handle-get [resource]
	(let [form (.. resource getResource getRequest getResourceRef getQueryAsForm)
		fresh (.equals "true" (.getFirstValue form "fresh"))]
		;(println form)
		;(println fresh)
		(with-connection (if fresh fresh-from-pool from-pool)
			(let [boards (get-boards)]
				;(println (get-board-max-timestamp))
				;(println boards)
				(if (empty? boards)
					nil
					(let [[board-list max-timestamp] (get-board-list boards)
						notes (get-notes)]
						;(println (str max-timestamp))
				    (if-not (nil? max-timestamp)
				    	(.setModificationTimestamp resource max-timestamp))
				    (json-str {:boards board-list :notes notes})))))))

(defn handle-get-info [resource]
	(with-connection from-pool
  	(get-board-max-timestamp)))

(defn handle-put [resource]
  ; Note: You can only "consume" the entity once, so if we want it
  ; as text, and want to refer to it more than once, we should keep
  ; a reference to that text.

  (let [text (.. resource getEntity (getText))
  	note (keyword-map (read-json text))]
		;(println note)
		(with-connection from-pool
			(let [note (add-note note)]
				;(println note)
    		(update-board-timestamp note))))
		    		
  (handle-get resource))