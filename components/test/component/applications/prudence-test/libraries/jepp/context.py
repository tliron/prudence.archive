# Helper to access the context attributes

import pickle

def get_context_attribute(conversation, name, get_default_value):
	value = conversation.getResource().getContext().getAttributes().get(name)
	if value is None:
		value = get_default_value()

		# Note: another thread might have changed our value in the meantime.
		# We'll make sure there is no duplication.
		
		existing = conversation.getResource().getContext().getAttributes().putIfAbsent(name, pickle.dumps(value))
		if not existing is None:
			value = pickle.loads(str(existing))
	else:
		value = pickle.loads(str(value))

	return value
