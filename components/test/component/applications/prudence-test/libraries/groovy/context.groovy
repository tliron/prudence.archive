// Helper to access the context attributes

getContextAttribute = { conversation, name, getDefaultValue ->
	def value = conversation.resource.context.attributes[name]
	if(value == null) {
		value = getDefaultValue()

		// Note: another thread might have changed our value in the meantime.
		// We'll make sure there is no duplication.

		def existing = conversation.resource.context.attributes.putIfAbsent(name, value)
		if(existing != null) {
			value = existing
		}
	}
	return value
}
