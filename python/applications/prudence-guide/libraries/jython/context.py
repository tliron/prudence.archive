# Helper to access the context attributes

def getContextAttribute(conversation, name, get_default_value):
	value = conversation.resource.context.attributes[name]
	if value == None:
		value = get_default_value()

		# Note: another thread might have changed our value in the meantime.
		# We'll make sure there is no duplication.

		existing = conversation.resource.context.attributes.putIfAbsent(name, value)
		if existing != None:
			value = existing

	return value
