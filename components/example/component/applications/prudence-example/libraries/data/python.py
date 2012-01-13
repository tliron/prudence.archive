
# Helper to access the application globals

def get_global(application, name, get_default_value):
	value = application.globals[name]
	if value == None:
		value = get_default_value()

		# Note: another thread might have changed our value in the meantime.
		# We'll make sure there is no duplication.

		existing = application.globals.putIfAbsent(name, value)
		if existing != None:
			value = existing

	return value
