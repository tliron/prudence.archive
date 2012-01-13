
// Helper to access the application globals

getGlobal = { name, getDefaultValue ->
	def value = application.globals[name]
	if (value == null) {
		value = getDefaultValue()

		// Note: another thread might have changed our value in the meantime.
		// We'll make sure there is no duplication.

		def existing = application.globals.putIfAbsent(name, value)
		if (existing != null) {
			value = existing
		}
	}
	return value
}
