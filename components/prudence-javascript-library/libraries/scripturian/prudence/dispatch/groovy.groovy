//
// This file is part of the Prudence Foundation Library
//
// Copyright 2011-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

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
