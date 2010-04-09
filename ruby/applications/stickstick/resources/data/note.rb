
require 'java'
import java.lang.System
import org.json.JSONObject

require $prudence.source.base_path.to_s + '/../libraries/stickstick/data.rb'

def merge key, a, b
	if !a.has_key?(key)
		return b[key]
	else
		return a[key]
	end
end

def get_id
    begin
        return Integer($prudence.resource.request.attributes['id'])
    except
    	return nil
    end

    #form = $prudence.resource.request.resource_ref.query_as_form
    #return Integer(form.get_first_value('id'))
end

def handle_init
    $prudence.add_media_type_by_name 'text/plain'
    $prudence.add_media_type_by_name 'application/json'
end

def handle_get
	id = get_id
	
    note = nil
    connection = get_connection
    begin
        note = get_note id, connection
        if note.nil?
        	return java.lang.Integer.new 404
        end
    ensure
    	connection.close
    end

    $prudence.modification_timestamp = note['timestamp']
    note.delete 'timestamp'
    return JSONObject.new note
end

def handle_get_info
	id = get_id
	
    note = nil
    connection = get_connection
    begin
        note = get_note id, connection
        if note.nil?
        	return nil
        end
    ensure
    	connection.close
    end

    return java.lang.Long.new note['timestamp']
end

def handle_post
	id = get_id

    # Note: You can only "consume" the entity once, so if we want it
    # as text, and want to refer to it more than once, we should keep
    # a reference to that text.
    
    text = $prudence.entity.text
    entity = JSONObject.new text
    note = {}
	for key in entity.keys
		note[key] = entity.get key
	end

    connection = get_connection
    begin
        existing = get_note id, connection
        if existing.nil?
        	return java.lang.Integer.new(404)
        end
        note = {
        	'id' => id,
        	'board' => merge('board', note, existing),
        	'x' => merge('x', note, existing),
        	'y' => merge('y', note, existing),
        	'size' => merge('size', note, existing),
        	'content' => merge('content', note, existing)
        }
        update_note note, connection
        update_board_timestamp note, connection
    ensure
    	connection.close
    end

    $prudence.modification_timestamp = note['timestamp']
    note.delete 'timestamp'
    return JSONObject.new note
end

def handle_delete
	id = get_id

    connection = get_connection
    begin 
        note = get_note id, connection
        if note.nil?
        	return java.lang.Integer.new 404
        end
        delete_note note, connection
        update_board_timestamp note, connection, System.current_time_millis
    ensure
    	connection.close
    end

    return nil
end
