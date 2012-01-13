
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.json.JSONObject

// Include the context library
document.executeOnce('/data/groovy/')

// State
//
// These make sure that our state is properly stored in the context,
// so that we always use the same state, even if this script is recompiled.

getStateLock = {
	return getGlobal('groovy.stateLock', {
		return new ReentrantReadWriteLock()
	})
}

getState = {
	return getGlobal('groovy.state', {
		return [name: 'Coraline', media: 'Film', rating: 'A+', characters: ['Coraline', 'Wybie', 'Mom', 'Dad']]
	})
}

setState = { value ->
	application.globals['groovy.state'] = value 
}

handleInit = { conversation ->
    conversation.addMediaTypeByName('application/json')
    conversation.addMediaTypeByName('text/plain')
}

handleGet = { conversation ->
	def r
	def stateLock = getStateLock()
	def state = getState()
	
	stateLock.readLock().lock()
	try {
		r = new JSONObject(state)
	}
	finally {
		stateLock.readLock().unlock()
	}
	
	return r
} 

handlePost = { conversation ->
	// Note that we are using the JSON library to parse the entity. While
	// a simple eval() would also work, the JSONObject constructor is much safer.
	
	def update = new JSONObject(conversation.entity.text)
	def stateLock = getStateLock()
	def state = getState()
	
	// Update our state
	stateLock.writeLock().lock()
	try {
		for(def key in update.keys()) {
			state[key] = update.get(key)
		}
	}
	finally {
		stateLock.writeLock().unlock()
	}
	
	return handleGet(conversation)
}

handlePut = { conversation ->
	// See comment in handlePost()

	def entity = new JSONObject(conversation.entity.text)

	def state = [:]
	for (def key in entity.keys()) {
		state[key] = entity.get(key)
	}
	
	setState(state)
	
	return handleGet(conversation)
}

handleDelete = { conversation ->
	setState([:])

	return null
}
