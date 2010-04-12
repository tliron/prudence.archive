
(defn get-connection
	([] (get-connection false))
	([fresh] nil))

(defn get-boards [connection] [{:id "Todo List" :timestamp 1234} {:id "Work" :timestamp 1235}])

(defn get-notes [connection] [])

(defn get-board-max-timestamp [connection] (System/currentTimeMillis))

(defn add-note [note connection])

(defn update-board-timestamp [note connection])
