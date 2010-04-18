; Helper to access the context attributes

(defn get-context-attribute [conversation name get-default-value]
	(let [value (.. conversation getResource getContext getAttributes (get name))]
		(if (nil? value)
			(let [value (get-default-value)]
		
				; Note: another thread might have changed our value in the meantime.
				; We'll make sure there is no duplication.
		
				(let [existing (.. conversation getResource getContext getAttributes (putIfAbsent name value))]
					(if (nil? existing)
						value
						existing)))
			value)))
