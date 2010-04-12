
(.. prudence (include "../libraries/stickstick/data/"))

(import
	'java.io.File)

(add-classpath (.toURL (File. (str (.. prudence getSource getBasePath) "/../libraries/clojure"))))
(use '[org.danlarkin.json :only (encode-to-str decode-from-str)])

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
		
		(let [connection (get-connection fresh)]
			(try
				(let [boards (get-boards connection)]
					(if (empty? boards)
						nil
						(let [[board-list max-timestamp] (get-board-list boards)
							notes (get-notes connection)]
							;(println (str max-timestamp))
					    (if (not (nil? max-timestamp))
					    	(.setModificationTimestamp prudence max-timestamp))
					    (encode-to-str {"boards" board-list "notes" notes}))))
		    (finally
		    	(if (not (nil? connection))
		    		(.close connection)))))))