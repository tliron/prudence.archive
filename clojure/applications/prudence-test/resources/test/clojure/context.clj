; Helper to access the context attributes

(defn get-context-attribute [name get-default-value]
	(def value (.. document getContainer getResource getContext getAttributes (get name)))
	(if (= value nil)
		(do
			(def value (get-default-value))
	
			; Note: another thread might have changed our value in the meantime.
			; We'll make sure there is no duplication.
	
			(def existing (.. document getContainer getResource getContext getAttributes (putIfAbsent name value)))
			(if (not= existing nil)
				(def value existing)
			)
		)
	)
	value
)
