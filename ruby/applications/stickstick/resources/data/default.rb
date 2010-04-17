
require 'java'
import org.json.JSONObject

require $prudence.source.base_path.to_s + '/../libraries/stickstick/data.rb'

def handle_init resource
    resource.add_media_type_by_name 'text/plain'
    resource.add_media_type_by_name 'application/json'
end

def handle_get resource
    form = resource.resource.request.resource_ref.query_as_form
    fresh = form.get_first_value('fresh') == 'true'
    
    max_timestamp = nil
    board_list = Array.new
    notes = nil

    connection = get_connection fresh
    begin
	    boards = get_boards connection
	    if !boards.nil?
		    for board in boards
		        board_list << board['id']
		        timestamp = board['timestamp']
		        if max_timestamp.nil? || (timestamp > max_timestamp)
		            max_timestamp = timestamp
		        end
		    end
	    else
	        return nil
	    end
	
	    notes = get_notes connection
    ensure
    	connection.close
    end

    if !max_timestamp.nil?
        resource.modification_timestamp = max_timestamp
    end
    return JSONObject.new({'boards' => board_list, 'notes' => notes})
end

def handle_get_info resource
    connection = get_connection
    begin
    	return java.lang.Long.new get_board_max_timestamp(connection)
    ensure
    	connection.close
    end
end

def handle_put resource
    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = resource.entity.text
    entity = JSONObject.new(text)
    note = {}
	for key in entity.keys
		note[key] = entity.get key
	end
    
    connection = get_connection
    begin
    	add_note note, connection
        update_board_timestamp note, connection
    ensure
    	connection.close
    end
    
    return handle_get resource
end