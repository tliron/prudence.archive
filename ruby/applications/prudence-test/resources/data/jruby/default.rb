#
# This script implements and handles a REST resource. Simply put, it is a state,
# addressed by a URL, that responds to verbs. Verbs represent logical operations
# on the state, such as create, read, update and delete (CRUD). They are primitive
# communications, which include very minimal session and no transaction state. As such,
# they are very straightforward to implement, and can lead to very scalable
# applications. 
#
# The exact URL of this resource depends on its its filename and/or its location in
# your directory structure. See your settings.rb for more information.
#

require 'java'
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.restlet.ext.json.JsonRepresentation
import org.json.JSONObject

# Include the context library

require @prudence.source.base_path.to_s + '/../libraries/jruby/context.rb'

# State
#
# These make sure that our state is properly stored in the context,
# so that we always use the same state, even if this script is recompiled.

def get_state_lock
	return get_context_attribute('jruby.stateLock') do
		ReentrantReadWriteLock.new
	end
end

def get_state
	return get_context_attribute('jruby.state') do
		{'name' => 'Coraline', 'media' => 'Film', 'rating' => 'A+', 'characters' => ['Coraline', 'Wybie', 'Mom', 'Dad']}
	end
end

def set_state value
	@prudence.resource.context.attributes['jruby.state'] = value
end

@state = get_state
@state_lock = get_state_lock

# This method is called when the resource is initialized. We will use it to set
# general characteristics for the resource.
	
def handleInit

	# The order in which we add the variants is their order of preference.
	# Note that clients often include a wildcard (such as "*/*") in the
	# "Accept" attribute of their request header, specifying that any media type
	# will do, in which case the first one we add will be used.

    @prudence.add_media_type_by_name 'application/json'
    @prudence.add_media_type_by_name 'text/plain'
	
end

# This method is called for the GET verb, which is expected to behave as a
# logical "read" of the resource's state.
#
# The expectation is that it return one representation, out of possibly many, of the
# resource's state. Returned values can be of any explicit sub-class of
# org.restlet.resource.Representation. Other types will be automatically converted to
# string representation using the client's requested media type and character set.
# These, and the language of the representation (defaulting to None), can be read and
# changed via @prudence.media_type, @prudence.character_set, and
# @prudence.language.
#
# Additionally, you can use @prudence.variant to interrogate the client's provided
# list of supported languages and encoding.

def handleGet

	r = nil
	state_lock = get_state_lock
	state = get_state

	state_lock.read_lock.lock
	begin
		r = JSONObject.new state
	ensure
		state_lock.read_lock.unlock
	end

	# Return a representation appropriate for the requested media type
	# of the possible options we created in handle_init()

	if @prudence.media_type_name == 'application/json'
		return JsonRepresentation.new r
	end

	return r
	
end

# This method is called for the POST verb, which is expected to behave as a
# logical "update" of the resource's state.
#
# The expectation is that prudence.entity represents an update to the state,
# that will affect future calls to handle_get(). As such, it may be possible
# to accept logically partial representations of the state.
#
# You may optionally return a representation, in the same way as handleGet().
# Because Ruby methods return the last statement's value by default,
# you must explicitly return a None if you do not want to return a representation
# to the client.

def handlePost

	update = JSONObject.new @prudence.entity.text
	state_lock = get_state_lock
	state = get_state
	
	# Update our state
	state_lock.write_lock.lock
	begin
		for key in update.keys
			state[key] = update.get key
		end
	ensure
		state_lock.write_lock.unlock
	end
	
	return handleGet
	
end

# This method is called for the PUT verb, which is expected to behave as a
# logical "create" of the resource's state.
#
# The expectation is that prudence.entity represents an entirely new state,
# that will affect future calls to handleGet(). Unlike handlePost(),
# it is expected that the representation be logically complete.
#
# You may optionally return a representation, in the same way as handleGet().
# Because Ruby methods return the last statement's value by default,
# you must explicitly return a nil if you do not want to return a representation
# to the client.

def handlePut

	update = JSONObject.new @prudence.entity.text

	state = {}	
	for key in update.keys
		state[key] = update.get key
	end
	
	set_state state
	
	return handleGet
	
end

# This method is called for the DELETE verb, which is expected to behave as a
# logical "delete" of the resource's state.
#
# The expectation is that subsequent calls to handleGet() will fail. As such,
# it doesn't make sense to return a representation, and any returned value will
# ignored. Still, it's a good idea to return nil to avoid any passing of value.

def handleDelete

	set_state({})

	return nil
	
end
