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

from java.util.concurrent.locks import ReentrantReadWriteLock
from org.restlet.ext.json import JsonRepresentation

# Include the context library

document.container.include('../libraries/jython/context')

# Include the minjson library
# (Note that we made a small change to minjson in order to accommodate Jython.
# See line 334 there.)

sys.path.append(str(document.container.source.basePath) + '/../libraries/jython/')
import minjson as json

# State
#
# These make sure that our state is properly stored in the context,
# so that we always use the same state, even if this script is recompiled.

def getStateLock():
	def createDefault():
		return ReentrantReadWriteLock()
	return getContextAttribute('jython.stateLock', createDefault)

def getState():
	def createDefault():
		return {'name': 'Coraline', 'media': 'Film', 'rating': 'A+', 'characters': ['Coraline', 'Wybie', 'Mom', 'Dad']}
	return getContextAttribute('jython.state', createDefault)

def setState(value):
	global document
	document.container.resource.context.attributes['jython.state'] = value

# This function is called when the resource is initialized. We will use it to set
# general characteristics for the resource.

def handleInit():
	# The order in which we add the variants is their order of preference.
	# Note that clients often include a wildcard (such as "*/*") in the
	# "Accept" attribute of their request header, specifying that any media type
	# will do, in which case the first one we add will be used.

    document.container.addMediaTypeByName('application/json')
    document.container.addMediaTypeByName('text/plain')

# This function is called for the GET verb, which is expected to behave as a
# logical "read" of the resource's state.
#
# The expectation is that it return one representation, out of possibly many, of the
# resource's state. Returned values can be of any explicit sub-class of
# org.restlet.resource.Representation. Other types will be automatically converted to
# string representation using the client's requested media type and character set.
# These, and the language of the representation (defaulting to None), can be read and
# changed via document.container.mediaType, document.container.characterSet, and
# document.container.language.
#
# Additionally, you can use document.container.variant to interrogate the client's provided
# list of supported languages and encoding.

def handleGet():
	r = None
	stateLock = getStateLock()
	state = getState()

	stateLock.readLock().lock()
	try:
		r = json.write(state)
	finally:
		stateLock.readLock().unlock()
	
	# Return a representation appropriate for the requested media type
	# of the possible options we created in handleInit()

	if document.container.mediaTypeName == 'application/json':
		r = JsonRepresentation(r)
		
	return r

# This function is called for the POST verb, which is expected to behave as a
# logical "update" of the resource's state.
#
# The expectation is that document.container.entity represents an update to the state,
# that will affect future calls to handleGet(). As such, it may be possible
# to accept logically partial representations of the state.
#
# You may optionally return a representation, in the same way as handleGet().
# Because Python functions return the last statement's value by default,
# you must explicitly return a None if you do not want to return a representation
# to the client.

def handlePost():
	# Note that we are using the minjson library to parse the entity. While
	# a simple eval() would also work, minjson.read() is much safer.

	update = json.read(document.container.entity.text)
	stateLock = getStateLock()
	state = getState()

	# Update our state
	stateLock.writeLock().lock()
	try:
		for key in update:
			state[key] = update[key]
	finally:
		stateLock.writeLock().unlock()

	return handleGet()

# This function is called for the PUT verb, which is expected to behave as a
# logical "create" of the resource's state.
#
# The expectation is that document.container.entity represents an entirely new state,
# that will affect future calls to handleGet(). Unlike handlePost(),
# it is expected that the representation be logically complete.
#
# You may optionally return a representation, in the same way as handleGet().
# Because Python functions return the last statement's value by default,
# you must explicitly return a None if you do not want to return a representation
# to the client.

def handlePut():
	# See comment in handlePost()

	update = json.read(document.container.entity.text)
	setState(update)

	return handleGet()

# This function is called for the DELETE verb, which is expected to behave as a
# logical "delete" of the resource's state.
#
# The expectation is that subsequent calls to handleGet() will fail. As such,
# it doesn't make sense to return a representation, and any returned value will
# ignored. Still, it's a good idea to return None to avoid any passing of value.

def handleDelete():
	setState({})

	return None
