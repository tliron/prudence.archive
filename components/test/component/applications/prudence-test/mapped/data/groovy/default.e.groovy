import java.util.concurrent.locks.ReentrantReadWriteLock
import org.restlet.ext.json.JsonRepresentation
import org.json.JSONObject

// Include the context library
document.execute('groovy/context/')

// State
//
// These make sure that our state is properly stored in the context,
// so that we always use the same state, even if this script is recompiled.

getStateLock = { conversation ->
	return getContextAttribute(conversation, 'groovy.stateLock', {
		return new ReentrantReadWriteLock()
	})
}

getState = { conversation ->
	return getContextAttribute(conversation, 'groovy.state', {
		return [name: 'Coraline', media: 'Film', rating: 'A+', characters: ['Coraline', 'Wybie', 'Mom', 'Dad']]
	})
}

setState = { conversation, value ->
	conversation.resource.context.attributes['groovy.state'] = value 
}

handleInit = { conversation ->
    conversation.addMediaTypeByName('application/json')
    conversation.addMediaTypeByName('text/plain')
}

handleGet = { conversation ->
	def r
	def stateLock = getStateLock(conversation)
	def state = getState(conversation)
	
	stateLock.readLock().lock()
	try {
		r = new JSONObject(state)
	}
	finally {
		stateLock.readLock().unlock()
	}
	
	// Return a representation appropriate for the requested media type
	// of the possible options we created in handleInit()

	if(conversation.mediaTypeName == 'application/json')
		r = new JsonRepresentation(r)

	return r
} 

handlePost = { conversation ->
	// Note that we are using the JSON library to parse the entity. While
	// a simple eval() would also work, the JSONObject constructor is much safer.
	
	def update = new JSONObject(conversation.entity.text)
	def stateLock = getStateLock(conversation)
	def state = getState(conversation)
	
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
	for(def key in entity.keys()) {
		state[key] = entity.get(key)
	}
	
	setState(conversation, state)
	
	return handleGet(conversation)
}

handleDelete = { conversation ->
	setState(conversation, [:])

	return null
}
