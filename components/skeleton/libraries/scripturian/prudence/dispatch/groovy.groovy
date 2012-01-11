
resources = [:]

document.executeOnce(application.globals['prudence.dispatch.groovy.library'])

handle = { conversation, method ->
	id = conversation.locals['prudence.id']
	resource = resources[id]
	if (resource == null) {
		conversation.statusCode = 404
		return null
	}
	try {
		return resource."$method"(conversation)
	}
	catch (MissingMethodException x) {
		conversation.statusCode = 405
		return null
	}
}

handleInit = { conversation ->
	handle(conversation, 'handleInit')
}

handleGet = { conversation ->
	return handle(conversation, 'handleGet')
}

handleGetInfo = { conversation ->
	return handle(conversation, 'handleGetInfo')
}

handlePost = { conversation ->
	return handle(conversation, 'handlePost')
}

handlePut = { conversation ->
	return handle(conversation, 'handlePut')
}

handleDelete = { conversation ->
	return handle(conversation, 'handleDelete')
}
