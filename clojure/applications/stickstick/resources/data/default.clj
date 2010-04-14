
(.. prudence (include "../libraries/stickstick/data/"))

(use
	'clojure.contrib.json.read
	'clojure.contrib.json.write
	'clojure.contrib.sql)

(import 'java.io.File)

(defn handle-init []
	(.. prudence (addMediaTypeByName "text/plain"))
	(.. prudence (addMediaTypeByName "application/json")))

; Returns [ids, max-timestamp]
(defn get-board-list [boards]
	(let [[board & others] boards]
		(if (empty? others)
			(if (nil? board)
				[[] nil]
				[[(board :id)] (board :timestamp)])
			(let [[other-ids others-max-timestamp] (get-board-list others)]
				[(cons (board :id) other-ids) (max (board :timestamp) others-max-timestamp)]))))

(defn handle-get []
	(let [form (.. prudence getResource getRequest getResourceRef getQueryAsForm)
		fresh (= (.getFirstValue form "fresh") "true")]
		
		(with-connection (if fresh fresh-from-pool from-pool)
			(let [boards (get-boards)]
				(if (empty? boards)
					nil
					(let [[board-list max-timestamp] (get-board-list boards)
						notes (get-notes)]
						;(println (str max-timestamp))
				    (if-not (nil? max-timestamp)
				    	(.setModificationTimestamp prudence max-timestamp))
				    (json-str {"boards" board-list "notes" notes})))))))

(defn handle-get-info []
	(with-connection from-pool
  	(get-board-max-timestamp)))

(defn handle-put []
  ; Note: You can only "consume" the entity once, so if we want it
  ; as text, and want to refer to it more than once, we should keep
  ; a reference to that text.
  
  (let [text (.. prudence getEntity (getText))
  	note (read-json text)]

		(with-connection from-pool
			(do
    		(add-note note)
    		(update-board-timestamp note))))
		    		
  (handle-get))