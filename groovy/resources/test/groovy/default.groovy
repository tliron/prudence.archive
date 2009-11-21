//
// This script implements and handles a REST resource. Simply put, it is a state,
// addressed by a URL, that responds to verbs. Verbs represent logical operations
// on the state, such as create, read, update and delete (CRUD). They are primitive
// communications, which include very minimal session and no transaction state. As such,
// they are very straightforward to implement, and can lead to very scalable
// applications. 
//
// The exact URL of this resource depends on its its filename and/or its location in
// your directory structure. See your prudence.conf for more information.
//

import java.util.concurrent.locks.ReentrantReadWriteLock
import org.restlet.data.MediaType
import org.restlet.representation.Variant
import org.restlet.ext.json.JsonRepresentation
import org.json.JSONObject

// Include the context library

document.container.include('groovy/context.groovy')

// State
//
// These make sure that our state is properly stored in the context,
// so that we always use the same state, even if this script is recompiled.

getStateLock = {
	return getContextAttribute('groovy.stateLock', {
		return new ReentrantReadWriteLock()
	})
}

getState = {
	return getContextAttribute('groovy.state', {
		return ['name': 'Coraline', 'media': 'Film', 'rating': 'A+', 'characters': ['Coraline', 'Wybie', 'Mom', 'Dad']]
	})
}

setState = { value ->
	document.container.resource.context.attributes['groovy.state'] = value 
}

// This closure is called when the resource is initialized. We will use it to set
// general characteristics for the resource.

handleInit = {
	// The order in which we add the variants is their order of preference.
	// Note that clients often include a wildcard (such as "*/*") in the
	// "Accept" attribute of their request header, specifying that any media type
	// will do, in which case the first one we add will be used.

	document.container.variants.add(new Variant(MediaType.APPLICATION_JSON))
	document.container.variants.add(new Variant(MediaType.TEXT_PLAIN))
}

// This closure is called for the GET verb, which is expected to behave as a
// logical "read" of the resource's state.
//
// The expectation is that it return one representation, out of possibly many, of the
// resource's state. Returned values can be of any explicit sub-class of
// org.restlet.resource.Representation. Other types will be automatically converted to
// string representation using the client's requested media type and character set.
// These, and the language of the representation (defaulting to null), can be read and
// changed via document.container.mediaType, document.container.characterSet, and
// document.container.language.
//
// Additionally, you can use document.container.variant to interrogate the client's provided
// list of supported languages and encoding.

handleGet = {
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
	
	// Return a representation appropriate for the requested media type
	// of the possible options we created in initializeResource()

	if(document.container.mediaType == MediaType.APPLICATION_JSON)
		r = new JsonRepresentation(r)

	return r
} 

// This closure is called for the POST verb, which is expected to behave as a
// logical "update" of the resource's state.
//
// The expectation is that document.container.entity represents an update to the state,
// that will affect future calls to handleGet(). As such, it may be possible
// to accept logically partial representations of the state.
//
// You may optionally return a representation, in the same way as handleGet().
// Because Groovy closures return the last statement's value by default,
// you must explicitly return a null if you do not want to return a representation
// to the client.

handlePost = {
	// Note that we are using the JSON library to parse the entity. While
	// a simple eval() would also work, the JSONObject constructor is much safer.
	
	def update = new JSONObject(document.container.entity.text)
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
	
	return handleGet()
}

// This closure is called for the PUT verb, which is expected to behave as a
// logical "create" of the resource's state.
//
// The expectation is that document.container.entity represents an entirely new state,
// that will affect future calls to handleGet(). Unlike handlePost(),
// it is expected that the representation be logically complete.
//
// You may optionally return a representation, in the same way as handleGet().
// Because Groovy closures return the last statement's value by default,
// you must explicitly return a null if you do not want to return a representation
// to the client.

handlePut = {
	// See comment in handlePost()

	def entity = new JSONObject(document.container.entity.text)

	def state = [:]
	for(def key in entity.keys()) {
		state[key] = entity.get(key)
	}
	
	setState(state)
	
	return handleGet()
}

// This closure is called for the DELETE verb, which is expected to behave as a
// logical "delete" of the resource's state.
//
// The expectation is that subsequent calls to handleGet() will fail. As such,
// it doesn't make sense to return a representation, and any returned value will
// ignored. Still, it's a good idea to return null to avoid any passing of value.

handleDelete = {

	setState([:])

	return null
}
