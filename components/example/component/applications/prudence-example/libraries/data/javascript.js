
// Helper to access the application globals

function getGlobal(name, getDefaultValue) {
	var value = application.globals.get(name)
	if (null === value) {
		value = getDefaultValue()

		// Note: another thread might have changed our value in the meantime.
		// We'll make sure there is no duplication.

		var existing = application.globals.putIfAbsent(name, value)
		if (null !== existing) {
			value = existing
		}
	}
	return value
}
