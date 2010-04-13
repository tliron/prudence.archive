
(defn connection-factory [params]
	(if (= (params :fresh) true) nil nil))
	
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
