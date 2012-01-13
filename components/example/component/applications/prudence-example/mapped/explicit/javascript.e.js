
importClass(java.util.concurrent.locks.ReentrantReadWriteLock)

document.executeOnce('/sincerity/json/')
document.executeOnce('/data/javascript/')

// State
//
// These make sure that our state is properly stored in the context,
// so that we always use the same state, even if this script is recompiled.

function getStateLock() {
	return getGlobal('javascript.stateLock', function() {
		return new ReentrantReadWriteLock()
	})
}

function getState() {
	return getGlobal('javascript.state', function() {
		return {name: 'Coraline', media: 'Film', rating: 'A+', characters: ['Coraline', 'Wybie', 'Mom', 'Dad']}
	})
}

function setState(value) {
	application.globals.put('javascript.state', value) 
}

function handleInit(conversation) {
    conversation.addMediaTypeByName('application/json')
    conversation.addMediaTypeByName('text/plain')
}

function handleGet(conversation) {
	var r
	var stateLock = getStateLock()
	var state = getState()
	
	stateLock.readLock().lock()
	try {
		r = Sincerity.JSON.to(state)
	}
	finally {
		stateLock.readLock().unlock()
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
	
	var update = Sincerity.JSON.from(String(conversation.entity.text))
	var stateLock = getStateLock()
	var state = getState()
	
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

	var update = Sincerity.JSON.from(String(conversation.entity.text))
	setState(update)
	
	return handleGet(conversation)
}

function handleDelete(conversation) {
	setState({})
	
	return null
}
