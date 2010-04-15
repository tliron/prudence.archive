
(.. prudence (include "../libraries/stickstick/shared/"))

(use 'clojure.contrib.sql)

(import
	'org.restlet.Application
	'java.sql.Timestamp
	'java.sql.Clob
	'com.threecrickets.prudence.util.MiniConnectionPoolManager)

(defn get-url [attributes]
	"jdbc:h2:data/h2/stickstick")

(defn get-data-source [attributes]
	(let [data-source (org.h2.jdbcx.JdbcDataSource.)]
		(.setURL data-source (get-url attributes))
		(.setUser data-source (get attributes "stickstick.username"))
		(.setPassword data-source (get attributes "stickstick.password"))
		data-source))
		
(defn create-connection-pool []
	;(println "new pool")
	(let [attributes (.. (Application/getCurrent) getContext getAttributes)]
		(MiniConnectionPoolManager. (get-data-source attributes) 10)))

(declare from-pool)
(declare add-board)
(declare add-note)

(defn get-connection [fresh]
	(.lock stickstick/connection-pool-lock)
	(try
		(when (or (nil? @stickstick/connection-pool) fresh)
			(if (nil? @stickstick/connection-pool)
				(compare-and-set! stickstick/connection-pool nil (create-connection-pool)))
			(with-connection from-pool
				(do
					(if fresh
						(do
							;(println "freshening")
							(do-commands
								"DROP TABLE board"
								"DROP TABLE note")))
					(do-commands
						"CREATE TABLE IF NOT EXISTS board (id VARCHAR(50) PRIMARY KEY, timestamp TIMESTAMP)"
						"CREATE TABLE IF NOT EXISTS note (id INT AUTO_INCREMENT PRIMARY KEY, board VARCHAR(50), x INT, y INT, size INT, content TEXT, timestamp TIMESTAMP)"
						"CREATE INDEX IF NOT EXISTS note_board_idx ON note (board)")
					(try
						(add-board {:id "Todo List"})
						(add-board {:id "Great Ideas"})
						(add-board {:id "Sandbox"})
						(add-note {:board "Sandbox" :x 50 :y 50 :size 1 :content "Clojure Rocks!"})
						;(catch Exception x (throw x))))))
						(catch Exception _)))))
		(.getConnection @stickstick/connection-pool)
		(finally
			(.unlock stickstick/connection-pool-lock))))

; DB specs for getting clojure.contrib.sql to use our connection pool
(defn connection-factory [params] (get-connection (params :fresh)))
(def from-pool {:factory connection-factory :fresh false})
(def fresh-from-pool {:factory connection-factory :fresh true})

(defn jsonable [o]
	(cond
		(map? o)
			(zipmap
				(keys o)
				(map jsonable (vals o)))
		(seq? o)
			(map jsonable o)
		(instance? Clob o)
			; clojure.contrib.sql doesn't convert clobs to strings, so we'll do it ourselves
			(.getSubString o 1 (.length o))
		(instance? Timestamp o)
			(.getTime o)
		:else
			o))

(defn keyword-map [m]
	(zipmap
		(map keyword (keys m))
		(vals m)))

(defn get-boards []
	(with-query-results rows ["SELECT id, timestamp FROM board"]
		(vec (jsonable rows))))

(defn add-board [board]
	(insert-records :board board))

(defn update-board-timestamp
	([note]
		(update-board-timestamp note (note :timestamp)))
	([note timestamp]
		(update-values :board ["id=?" (note :board)] {:timestamp (Timestamp. timestamp)})))

(defn get-board-max-timestamp []
	(with-query-results rows ["SELECT MAX(timestamp) FROM board"]
		(nth (vals (first rows)) 0)))

(defn get-note [id] 
	(with-query-results rows ["SELECT id, board, x, y, size, content, timestamp FROM note WHERE id=?" id]
		(jsonable (first rows))))

(defn get-notes []
	(with-query-results rows ["SELECT id, board, x, y, size, content FROM note"]
		(vec (jsonable rows))))

(defn add-note [note]
	(let [note (assoc note :timestamp (Timestamp. (System/currentTimeMillis)))]
		(insert-records :note note)
		(jsonable note)))

(defn update-note [note]
	(let [note (assoc note :timestamp (Timestamp. (System/currentTimeMillis)))]
		(update-values :note ["id=?" (note :id)] (dissoc (dissoc note :id) :board))
		(jsonable note)))

(defn delete-note [note]
	(delete-rows :note ["id=?" (note :id)]))
