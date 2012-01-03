import sys

from java.util.concurrent.locks import ReentrantReadWriteLock
from org.restlet.ext.json import JsonRepresentation
from jython.context import *

import jython.minjson as json

# (Note that we made a small change to minjson in order to accommodate Jython.
# See line 334 there.)

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

def handle_init(conversation):
    conversation.addMediaTypeByName('application/json')
    conversation.addMediaTypeByName('text/plain')

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

def handle_post(conversation):
	# Note that we are using the minjson library to parse the entity. While
	# a simple eval() would also work, minjson.read() is safer.

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

def handle_put(conversation):
	# See comment in handle_post()

	update = json.read(conversation.entity.text)
	set_state(conversation, update)

	return handle_get(conversation)

def handle_delete(conversation):
	set_state(conversation, {})

	return None
