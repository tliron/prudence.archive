
# Helper to access the application globals

import pickle

def get_global(application, name, get_default_value):
	value = application.getGlobals().get(name)
	if value is None:
		value = get_default_value()

		# Note: another thread might have changed our value in the meantime.
		# We'll make sure there is no duplication.
		
		existing = application.getGlobals().putIfAbsent(name, pickle.dumps(value))
		if not existing is None:
			value = pickle.loads(str(existing))
	else:
		value = pickle.loads(str(value))

	return value
