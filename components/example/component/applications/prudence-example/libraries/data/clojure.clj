
; Helper to access the application globals

(ns data.clojure)

(defn get-global [application name get-default-value]
	(let [value (.. application getGlobals (get name))]
		(if (nil? value)
			(let [value (get-default-value)]
		
				; Note: another thread might have changed our value in the meantime.
				; We'll make sure there is no duplication.
		
				(let [existing (.. application getGlobals (putIfAbsent name value))]
					(if (nil? existing)
						value
						existing)))
			value)))
