
document.execute('/stickstick/data/')
document.execute('/sincerity/json/')

importClass(java.lang.System)

function merge(key, a, b) {
	if(typeof(a[key]) == 'undefined') {
		return b[key]
	}
	else {
		return a[key]
	}
}

function getId(conversation) {
    try {
        return parseInt(conversation.locals.get('id'))
    }
    catch(e) {
    	return null
    }

    //return parseInt(conversation.query.get('id'))
}

function handleInit(conversation) {
    conversation.addMediaTypeByName('text/plain')
    conversation.addMediaTypeByName('application/json')
}

function handleGet(conversation) {
	var id = getId(conversation)
	
    var note
    var connection = new Stickstick.Connection()
    try {
        note = connection.getNote(id)
        if(note == null) {
        	return 404
        }
    }
    finally {
    	connection.close()
    }

    conversation.modificationTimestamp = note.timestamp
    delete note.timestamp
    return Sincerity.JSON.to(note)
}

function handleGetInfo(conversation) {
	var id = getId(conversation)
	
    var note
    var connection = new Stickstick.Connection()
    try {
        note = connection.getNote(id)
        if(note == null) {
        	return null
        }
    }
    finally {
    	connection.close()
    }

    return note.timestamp
}

function handlePost(conversation) {
	var id = getId(conversation)

    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    var text = conversation.entity.text
    var note = Sincerity.JSON.from(String(text))

    var connection = new Stickstick.Connection()
    try {
        var existing = connection.getNote(id)
        if(existing == null) {
        	return 404
        }
        note = {
        	id: id,
        	board: merge('board', note, existing),
        	x: merge('x', note, existing),
        	y: merge('y', note, existing),
        	size: merge('size', note, existing),
        	content: merge('content', note, existing)
        }
        connection.updateNote(note)
        connection.updateBoardTimestamp(note)
    }
    finally {
    	connection.close()
    }

    conversation.modificationTimestamp = note.timestamp
    delete note.timestamp
    return Sincerity.JSON.to(note)
}

function handleDelete(conversation) {
	var id = getId(conversation)

    var connection = new Stickstick.Connection()
    try {
        var note = connection.getNote(id)
        if(note == null) {
        	return 404
        }
        connection.deleteNote(note)
        connection.updateBoardTimestamp(note, System.currentTimeMillis())
    }
    finally {
    	connection.close()
    }

    return null
}
