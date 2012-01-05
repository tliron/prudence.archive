
document.execute('/stickstick/data/')
document.execute('/sincerity/json/')

function handleInit(conversation) {
    conversation.addMediaTypeByName('text/plain')
    conversation.addMediaTypeByName('application/json')
}

function handleGet(conversation) {
    var fresh = conversation.query.get('fresh') == 'true'
    
    var maxTimestamp
    var boardList = []
    var notes

    var connection = new Stickstick.Connection(fresh)
    try {
	    var boards = connection.getBoards()
	    if(boards != null) {
		    for(var i in boards) {
		    	var board = boards[i]
		        boardList.push(board.id)
		        var timestamp = board.timestamp
		        if((maxTimestamp == null) || (timestamp > maxTimestamp)) {
		            maxTimestamp = timestamp
		        }
		    }
	    }
	    else {
	        return null
	    }
	
	    notes = connection.getNotes()
    }
    finally {
    	connection.close()
    }

    if(maxTimestamp != null) {
        conversation.modificationTimestamp = maxTimestamp
    }
    return Sincerity.JSON.to({boards: boardList, notes: notes})
}

function handleGetInfo(conversation) {
    var connection = new Stickstick.Connection()
    try {
    	return connection.getBoardMaxTimestamp()
    }
    finally {
    	connection.close()
    }
}

function handlePut(conversation) {
    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    var text = conversation.entity.text
    var note = Sincerity.JSON.from(String(text))
    
    var connection = new Stickstick.Connection()
    try {
    	connection.addNote(note)
        connection.updateBoardTimestamp(note)
    }
    finally {
    	connection.close()
    }
    
    return handleGet(conversation)
}