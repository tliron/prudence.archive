
;(.. prudence (execute "../libraries/stickstick.shared/shared/"))

(ns stickstick.data)

(use
	'clojure.contrib.sql
	'stickstick.shared)

(import
	'java.sql.Timestamp
	'java.sql.Clob
  'java.io.StringWriter
  'org.apache.commons.pool.impl.GenericObjectPool
  'org.apache.commons.dbcp.DataSourceConnectionFactory
  'org.apache.commons.dbcp.PoolableConnectionFactory
  'org.apache.commons.dbcp.PoolingDataSource)

; clojure.contrib.sql annoyingly prints exceptions to *err* 
(defmacro with-connection-silent [db-spec & body]
  `(binding [*err* (StringWriter.)]
    (with-connection ~db-spec ~@body)))

(defn get-url [application]
  (let [url (str "jdbc:" (.. application getGlobals (get "stickstick.backend")) ":")]
    (let [url
          (if (not (= (.. application getGlobals (get "stickstick.host")) ""))
            (if (= (.. application getGlobals (get "stickstick.backend")) "h2")
              (str url "tcp:")
              (str url "//" (.. application getGlobals (get "stickstick.host")) "/"))
            url)]
      (let [url
            (if (not (= (.. application getGlobals (get "stickstick.database")) ""))
              (str url (.. application getGlobals (get "stickstick.database")))
              url)]
        (if (= (.. application getGlobals (get "stickstick.backend")) "h2")
          (str url ";MVCC=TRUE")
          url)))))

(defn get-data-source [application]
	(let [data-source (org.h2.jdbcx.JdbcDataSource.)]
		(.setURL data-source (get-url application))
		(.setUser data-source (.. application getGlobals (get "stickstick.username")))
		(.setPassword data-source (.. application getGlobals (get "stickstick.password")))
		data-source))
		
(defn create-connection-pool [application]
  (let [connection-pool (GenericObjectPool. nil 10)]
    (PoolableConnectionFactory. (DataSourceConnectionFactory. (get-data-source application)) connection-pool nil nil false true)
    (PoolingDataSource. connection-pool)))

(declare from-pool)
(declare add-board)
(declare add-note)

(defn get-connection [application fresh]
	(.lock stickstick.shared/connection-pool-lock)
	(try
		(when (or (nil? @stickstick.shared/connection-pool) fresh)
			(if (nil? @stickstick.shared/connection-pool)
				(compare-and-set! stickstick.shared/connection-pool nil (create-connection-pool application)))
			(with-connection-silent (from-pool application) 
				(do
					(if fresh
						(do
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
						(catch Exception _)))))
		(.getConnection @stickstick.shared/connection-pool)
		(finally
			(.unlock stickstick.shared/connection-pool-lock))))

; DB specs for getting clojure.contrib.sql to use our connection pool
(defn connection-factory [params] (get-connection (params :application) (params :fresh)))
(defn from-pool [application] {:factory connection-factory :application application :fresh false})
(defn fresh-from-pool [application] {:factory connection-factory :application application :fresh true})

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
