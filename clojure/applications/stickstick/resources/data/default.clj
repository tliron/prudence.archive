
(use
	'clojure.contrib.json.read
	'clojure.contrib.json.write
	'clojure.contrib.sql
	'stickstick.data)

(import 'java.io.File)

(defn handle-init [conversation]
	(.. conversation (addMediaTypeByName "text/plain"))
	(.. conversation (addMediaTypeByName "application/json")))

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

(defn handle-get [conversation]
	(let [fresh (.equals "true" (.. conversation getQuery (get "fresh")))]
		;(println form)
		;(println fresh)
		(with-connection (if fresh (fresh-from-pool application) (from-pool application))
			(let [boards (get-boards)]
				;(println (get-board-max-timestamp))
				;(println boards)
				(if (empty? boards)
					nil
					(let [[board-list max-timestamp] (get-board-list boards)
						notes (get-notes)]
						;(println (str max-timestamp))
				    (if-not (nil? max-timestamp)
				    	(.setModificationTimestamp conversation max-timestamp))
				    (json-str {:boards board-list :notes notes})))))))

(defn handle-get-info [conversation]
	(with-connection from-pool
  	(get-board-max-timestamp)))

(defn handle-put [conversation]
  ; Note: You can only "consume" the entity once, so if we want it
  ; as text, and want to refer to it more than once, we should keep
  ; a reference to that text.

  (let [text (.. conversation getEntity (getText))
  	note (keyword-map (read-json text))]
		;(println note)
		(with-connection (from-pool application)
			(let [note (add-note note)]
				;(println note)
    		(update-board-timestamp note))))
		    		
  (handle-get conversation))