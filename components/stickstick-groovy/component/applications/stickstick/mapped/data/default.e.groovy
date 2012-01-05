
document.execute('stickstick/data/')

import org.json.JSONObject

handleInit = { conversation ->
    conversation.addMediaTypeByName('text/plain')
    conversation.addMediaTypeByName('application/json')
}

handleGet = { conversation ->
    def fresh = conversation.query.fresh == 'true'
    
    def maxTimestamp
    def boardList = []
    def notes

    def connection = getConnection(fresh)
    try {
	    def boards = getBoards(connection)
	    if(boards != null) {
		    for(def board in boards) {
		        boardList.push(board.id)
		        def timestamp = board.timestamp
		        if((maxTimestamp == null) || (timestamp > maxTimestamp)) {
		            maxTimestamp = timestamp
		        }
		    }
	    }
	    else {
	        return null
	    }
	
	    notes = getNotes(connection)
    }
    finally {
    	connection.close()
    }

    if(maxTimestamp != null) {
        conversation.modificationTimestamp = maxTimestamp
    }
    return new JSONObject([boards: boardList, notes: notes])
}

handleGetInfo = { conversation ->
    def connection = getConnection()
    try {
    	return getBoardMaxTimestamp(connection)
    }
    finally {
    	connection.close()
    }
}

handlePut = { conversation ->
    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    def text = conversation.entity.text
    def entity = new JSONObject(text)
    def note = [:]
	for(def key in entity.keys()) {
		note[key] = entity.get(key)
	}
    
    def connection = getConnection()
    try {
    	addNote(note, connection)
        updateBoardTimestamp(note, connection)
    }
    finally {
    	connection.close()
    }
    
    return handleGet(conversation)
}