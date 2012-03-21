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

var resources = {}

document.executeOnce(application.globals.get('prudence.dispatch.javascript.library'))

function handle(conversation, method) {
	var id = conversation.locals.get('prudence.id')
	var resource = resources[id]
	if (undefined === resource) {
		conversation.statusCode = 404
		return null
	}
	method = resource[method]
	if (undefined === method) {
		conversation.statusCode = 405
		return null
	}
	return method(conversation)
}

function handleInit(conversation) {
	handle(conversation, 'handleInit')
}

function handleGet(conversation) {
	return handle(conversation, 'handleGet')
}

function handleGetInfo(conversation) {
	return handle(conversation, 'handleGetInfo')
}

function handlePost(conversation) {
	return handle(conversation, 'handlePost')
}

function handlePut(conversation) {
	return handle(conversation, 'handlePut')
}

function handleDelete(conversation) {
	return handle(conversation, 'handleDelete')
}