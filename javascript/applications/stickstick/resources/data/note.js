
prudence.include('../libraries/stickstick/data/');
prudence.include('../libraries/json2/');

importClass(java.lang.System);

function merge(key, a, b) {
	if(typeof(a[key]) == 'undefined') {
		return b[key];
	}
	else {
		return a[key];
	}
}

function getId() {
    try {
        return parseInt(prudence.resource.request.attributes.get('id'));
    }
    catch(e) {
    	return null;
    }

    //var form = prudence.resource.request.resourceRef.queryAsForm;
    //return parseInt(form.getFirstValue('id'));
}

function handleInit() {
    prudence.addMediaTypeByName('text/plain');
    prudence.addMediaTypeByName('application/json');
}

function handleGet() {
	var id = getId();
	
    var note;
    var connection = getConnection();
    try {
        note = getNote(id, connection);
        if(note == null) {
        	return 404;
        }
    }
    finally {
    	connection.close();
    }

    prudence.modificationTimestamp = note.timestamp;
    delete note.timestamp;
    return JSON.stringify(note);
}

function handleGetInfo() {
	var id = getId();
	
    var note;
    var connection = getConnection();
    try {
        note = getNote(id, connection);
        if(note == null) {
        	return null;
        }
    }
    finally {
    	connection.close();
    }

    return note.timestamp;
}

function handlePost() {
	var id = getId();

    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    var text = prudence.entity.text;
    var note = JSON.parse(String(text));

    var connection = getConnection();
    try {
        var existing = getNote(id, connection);
        if(existing == null) {
        	return 404;
        }
        note = {
        	id: id,
        	board: merge('board', note, existing),
        	x: merge('x', note, existing),
        	y: merge('y', note, existing),
        	size: merge('size', note, existing),
        	content: merge('content', note, existing)
        };
        updateNote(note, connection);
        updateBoardTimestamp(note, connection);
    }
    finally {
    	connection.close();
    }

    prudence.modificationTimestamp = note.timestamp;
    delete note.timestamp;
    return JSON.stringify(note);
}

function handleDelete() {
	var id = getId();

    var connection = getConnection();
    try {
        var note = getNote(id, connection);
        if(note == null) {
        	return 404;
        }
        deleteNote(note, connection);
        updateBoardTimestamp(note, connection, System.currentTimeMillis());
    }
    finally {
    	connection.close();
    }

    return null;
}
