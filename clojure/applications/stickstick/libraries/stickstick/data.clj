
(.. prudence (include "../libraries/stickstick/shared/"))

(use 'clojure.contrib.sql)

(import
	'org.restlet.Application
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
	(println "new pool")
	(let [attributes (.. (Application/getCurrent) getContext getAttributes)]
		(MiniConnectionPoolManager. (get-data-source attributes) 10)))

(declare from-pool)

(defn get-connection [fresh]
	(.lock stickstick/connection-pool-lock)
	(try
		(when (or (nil? @stickstick/connection-pool) fresh)
			(if (nil? @stickstick/connection-pool)
				(compare-and-set! stickstick/connection-pool nil (create-connection-pool)))
			(with-connection from-pool
				(if fresh
					(do-commands
						"DROP TABLE board"
						"DROP TABLE note"))
				(do-commands
					"CREATE TABLE IF NOT EXISTS board (id VARCHAR(50) PRIMARY KEY, timestamp TIMESTAMP)"
					"CREATE TABLE IF NOT EXISTS note (id INT AUTO_INCREMENT PRIMARY KEY, board VARCHAR(50), x INT, y INT, size INT, content TEXT, timestamp TIMESTAMP)"
					"CREATE INDEX IF NOT EXISTS note_board_idx ON note (board)")))
		(.getConnection @stickstick/connection-pool)
		(finally
			(.unlock stickstick/connection-pool-lock))))

; DB specs for getting clojure.contrib.sql to use our connection pool
(defn connection-factory [params] (get-connection (params :fresh)))
(def from-pool {:factory connection-factory :fresh false})
(def from-pool-fresh {:factory connection-factory :fresh true})

(defn get-boards [] [{:id "Todo List" :timestamp 1234} {:id "Work" :timestamp 1235}])

(defn add-board [board])

(defn update-board-timestamp [note])

(defn get-board-max-timestamp [] (System/currentTimeMillis))

(defn get-note [id] {:id 1 :board "Todo List" :x 50 :y 50 :size 1 :content "Test" :timestamp 1235})

(defn get-notes [] [(get-note 1)])

(defn add-note [note])

(defn update-note [note])

(defn delete-note [note])
