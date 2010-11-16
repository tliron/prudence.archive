importClass(
	java.util.concurrent.locks.ReentrantReadWriteLock,
	org.restlet.ext.json.JsonRepresentation)

// Include the context library
document.execute('rhino/context/')

// Include the JSON library
document.execute('rhino/json2/')

// State
//
// These make sure that our state is properly stored in the context,
// so that we always use the same state, even if this script is recompiled.

function getStateLock(conversation) {
	return getContextAttribute(conversation, 'rhino.stateLock', function() {
		return new ReentrantReadWriteLock()
	})
}

function getState(conversation) {
	return getContextAttribute(conversation, 'rhino.state', function() {
		return {name: 'Coraline', media: 'Film', rating: 'A+', characters: ['Coraline', 'Wybie', 'Mom', 'Dad']}
	})
}

function setState(conversation, value) {
	conversation.resource.context.attributes.put('rhino.state', value) 
}

function handleInit(conversation) {
    conversation.addMediaTypeByName('application/json')
    conversation.addMediaTypeByName('text/plain')
}

function handleGet(conversation) {
	var r
	var stateLock = getStateLock(conversation)
	var state = getState(conversation)
	
	stateLock.readLock().lock()
	try {
		r = JSON.stringify(state)
	}
	finally {
		stateLock.readLock().unlock()
	}
	
	// Return a representation appropriate for the requested media type
	// of the possible options we created in handleInit()

	if(conversation.mediaTypeName == 'application/json') {
		r = new JsonRepresentation(r)
	}
	
	return r
}

function handlePost(conversation) {
	// Note that we are using the JSON library to parse the entity. While
	// a simple eval() would also work, JSON.parse() is safer.
	// Note, too, that we are using String() to translate the Java string
	// into a JavaScript string. In many cases this is unnecessary, but
	// in this case the JSON library specifically expects a JavaScript string
	// object.
	
	var update = JSON.parse(String(conversation.entity.text))
	var stateLock = getStateLock(conversation)
	var state = getState(conversation)
	
	stateLock.writeLock().lock()
	try {
		for(var key in update) {
			state[key] = update[key]
		}
	}
	finally {
		stateLock.writeLock().unlock()
	}
	
	return handleGet(conversation)
}

function handlePut(conversation) {
	// See comment in handlePost()

	var update = JSON.parse(String(conversation.entity.text))
	setState(conversation, update)
	
	return handleGet(conversation)
}

function handleDelete(conversation) {
	setState(conversation, {})
	
	return null
}
