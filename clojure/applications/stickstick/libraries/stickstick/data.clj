
(require 'clojure.contrib.sql)

(defn get-connection
	([] (get-connection false))
	([fresh] nil))

(defn get-boards [connection] [{:id "Todo List" :timestamp 1234} {:id "Work" :timestamp 1235}])

(defn add-board [board connection])

(defn update-board-timestamp [note connection])

(defn get-board-max-timestamp [connection] (System/currentTimeMillis))

(defn get-note [id connection] {:id 1 :board "Todo List" :x 50 :y 50 :size 1 :content "Test" :timestamp 1235})

(defn get-notes [connection] [(get-note 1 connection)])

(defn add-note [note connection])

(defn update-note [note connection])

(defn delete-note [note connection])
