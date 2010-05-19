#
# This script implements and handles a REST resource. Simply put, it is a state,
# addressed by a URL, that responds to verbs. Verbs represent logical operations
# on the state, such as create, read, update and delete (CRUD). They are primitive
# communications, which include very minimal session and no transaction state. As such,
# they are very straightforward to implement, and can lead to very scalable
# applications. 
#
# The exact URL of this resource depends on its its filename and/or its location in
# your directory structure. See your settings.py for more information.
#

import sys

from java.util.concurrent.locks import ReentrantReadWriteLock
from org.restlet.ext.json import JsonRepresentation

# Include the context library

document.execute('../libraries/jython/context/')

# Include the minjson library
# (Note that we made a small change to minjson in order to accommodate Jython.
# See line 334 there.)

import jython.minjson as json

# State
#
# These make sure that our state is properly stored in the context,
# so that we always use the same state, even if this script is recompiled.

def get_state_lock(conversation):
	def create_default():
		return ReentrantReadWriteLock()
	return get_context_attribute(conversation, 'jython.stateLock', create_default)

def get_state(conversation):
	def create_default():
		return {'name': 'Coraline', 'media': 'Film', 'rating': 'A+', 'characters': ['Coraline', 'Wybie', 'Mom', 'Dad']}
	return get_context_attribute(conversation, 'jython.state', create_default)

def set_state(conversation, value):
	conversation.resource.context.attributes['jython.state'] = value

# This function is called when the resource is initialized. We will use it to set
# general characteristics for the resource.

def handle_init(conversation):
	# The order in which we add the variants is their order of preference.
	# Note that clients often include a wildcard (such as "*/*") in the
	# "Accept" attribute of their request header, specifying that any media type
	# will do, in which case the first one we add will be used.

    conversation.addMediaTypeByName('application/json')
    conversation.addMediaTypeByName('text/plain')

# This function is called for the GET verb, which is expected to behave as a
# logical "read" of the resource's state.
#
# The expectation is that it return one representation, out of possibly many, of the
# resource's state. Returned values can be of any explicit sub-class of
# org.restlet.resource.Representation. Other types will be automatically converted to
# string representation using the client's requested media type and character set.
# These, and the language of the representation (defaulting to None), can be read and
# changed via conversation.mediaType, conversation.characterSet, and
# conversation.language.
#
# Additionally, you can use conversation.variant to interrogate the client's provided
# list of supported languages and encoding.

def handle_get(conversation):
	r = None
	state_lock = get_state_lock(conversation)
	state = get_state(conversation)

	state_lock.readLock().lock()
	try:
		r = json.write(state)
	finally:
		state_lock.readLock().unlock()
	
	# Return a representation appropriate for the requested media type
	# of the possible options we created in handle_init()

	if conversation.mediaTypeName == 'application/json':
		r = JsonRepresentation(r)
		
	return r

# This function is called for the POST verb, which is expected to behave as a
# logical "update" of the resource's state.
#
# The expectation is that conversation.entity represents an update to the state,
# that will affect future calls to handle_get(). As such, it may be possible
# to accept logically partial representations of the state.
#
# You may optionally return a representation, in the same way as handle_get().
# Because Python functions return the last statement's value by default,
# you must explicitly return a None if you do not want to return a representation
# to the client.

def handle_post(conversation):
	# Note that we are using the minjson library to parse the entity. While
	# a simple eval() would also work, minjson.read() is much safer.

	update = json.read(conversation.entity.text)
	state_lock = get_state_lock(conversation)
	state = get_state(conversation)

	# Update our state
	state_lock.writeLock().lock()
	try:
		for key in update:
			state[key] = update[key]
	finally:
		state_lock.writeLock().unlock()

	return handle_get(conversation)

# This function is called for the PUT verb, which is expected to behave as a
# logical "create" of the resource's state.
#
# The expectation is that conversation.entity represents an entirely new state,
# that will affect future calls to handle_get(). Unlike handle_post(),
# it is expected that the representation be logically complete.
#
# You may optionally return a representation, in the same way as handle_get().
# Because Python functions return the last statement's value by default,
# you must explicitly return a None if you do not want to return a representation
# to the client.

def handle_put(conversation):
	# See comment in handle_post()

	update = json.read(conversation.entity.text)
	set_state(conversation, update)

	return handle_get(conversation)

# This function is called for the DELETE verb, which is expected to behave as a
# logical "delete" of the resource's state.
#
# The expectation is that subsequent calls to handle_get() will fail. As such,
# it doesn't make sense to return a representation, and any returned value will
# ignored. Still, it's a good idea to return None to avoid any passing of value.

def handle_delete(conversation):
	set_state(conversation, {})

	return None
