
prudence.include('../libraries/stickstick/data/');
prudence.include('../libraries/json2/');

function handleInit(conversation) {
    conversation.addMediaTypeByName('text/plain');
    conversation.addMediaTypeByName('application/json');
}

function handleGet(conversation) {
    var form = conversation.resource.request.resourceRef.queryAsForm;
    var fresh = form.getFirstValue('fresh') == 'true';
    
    var maxTimestamp;
    var boardList = [];
    var notes;

    var connection = getConnection(fresh);
    try {
	    var boards = getBoards(connection);
	    if(boards != null) {
		    for(var i in boards) {
		    	var board = boards[i];
		        boardList.push(board.id);
		        var timestamp = board.timestamp;
		        if((maxTimestamp == null) || (timestamp > maxTimestamp)) {
		            maxTimestamp = timestamp;
		        }
		    }
	    }
	    else {
	        return null;
	    }
	
	    notes = getNotes(connection);
    }
    finally {
    	connection.close();
    }

    if(maxTimestamp != null) {
        conversation.modificationTimestamp = maxTimestamp;
    }
    return JSON.stringify({boards: boardList, notes: notes});
}

function handleGetInfo(conversation) {
    var connection = getConnection();
    try {
    	return getBoardMaxTimestamp(connection);
    }
    finally {
    	connection.close();
    }
}

function handlePut(conversation) {
    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    var text = conversation.entity.text;
    var note = JSON.parse(String(text));
    
    var connection = getConnection();
    try {
    	addNote(note, connection);
        updateBoardTimestamp(note, connection);
    }
    finally {
    	connection.close();
    }
    
    return handleGet(conversation);
}