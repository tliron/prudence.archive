require 'java'
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.restlet.ext.json.JsonRepresentation
import org.json.JSONObject

# Include the context library

require 'jruby/context.rb'

# State
#
# These make sure that our state is properly stored in the context,
# so that we always use the same state, even if this script is recompiled.

def get_state_lock conversation
	return get_context_attribute(conversation, 'jruby.stateLock') do
		ReentrantReadWriteLock.new
	end
end

def get_state conversation
	return get_context_attribute(conversation, 'jruby.state') do
		{'name' => 'Coraline', 'media' => 'Film', 'rating' => 'A+', 'characters' => ['Coraline', 'Wybie', 'Mom', 'Dad']}
	end
end

def set_state conversation, value
	conversation.resource.context.attributes['jruby.state'] = value
end

def handle_init conversation
    conversation.add_media_type_by_name 'application/json'
    conversation.add_media_type_by_name 'text/plain'
end

def handle_get conversation
	r = nil
	state_lock = get_state_lock conversation
	state = get_state conversation

	state_lock.read_lock.lock
	begin
		r = JSONObject.new state
	ensure
		state_lock.read_lock.unlock
	end

	# Return a representation appropriate for the requested media type
	# of the possible options we created in handle_init()

	if conversation.media_type_name == 'application/json'
		return JsonRepresentation.new r
	end

	return r
end

def handle_post conversation
	update = JSONObject.new conversation.entity.text
	state_lock = get_state_lock conversation
	state = get_state conversation
	
	# Update our state
	state_lock.write_lock.lock
	begin
		for key in update.keys
			state[key] = update.get key
		end
	ensure
		state_lock.write_lock.unlock
	end
	
	return handle_get conversation
end

# This method is called for the PUT verb, which is expected to behave as a
# logical "create" of the resource's state.
#
# The expectation is that conversation.entity represents an entirely new state,
# that will affect future calls to handle_get(). Unlike handle_post(),
# it is expected that the representation be logically complete.
#
# You may optionally return a representation, in the same way as handle_get().
# Because Ruby methods return the last statement's value by default,
# you must explicitly return a nil if you do not want to return a representation
# to the client.

def handle_put conversation
	update = JSONObject.new conversation.entity.text

	state = {}	
	for key in update.keys
		state[key] = update.get key
	end
	
	set_state conversation, state
	
	return handle_get conversation	
end

def handle_delete conversation
	set_state(conversation, {})

	return nil
end
