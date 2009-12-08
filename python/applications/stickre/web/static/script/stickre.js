
function clear() {
	$('div.stickynote').remove();
}

function show(notes) {
	clear();
	for(var i in notes) {
		notes[i].containment = 'content';
		notes[i].size = 'small';
		notes[i].ontop = true;
		notes[i].ondelete = destroy;
		notes[i].onstop = move;
		$('#content').stickynote.createNote(notes[i]);
	}
}

function create(note, text) {
	var pos = note.offset();
	var ppos = note.parent().offset();
	text = text.replace('\n', '<br />'); // JSON is unhappy with newlines
	$.ajax({
		type: 'put',
		url: 'notes/',
		dataType: 'json',
		contentType: 'application/json',
		data: JSON.stringify({content: text, x: pos.left - ppos.left, y: pos.top - ppos.top}),
		success: show,
		error: fail
	});
}

function move(event, note) {
	var id = note.helper.attr('noteid');
	if(id) {
		var pos = note.position;
		$.ajax({
			type: 'post',
			url: 'note/' + id +'/',
			dataType: 'json',
			contentType: 'application/json',
			data: JSON.stringify({x: pos.left, y: pos.top}),
			success: refresh,
			error: fail
		});
	}
}

function destroy(note, obj) {
	var id = obj.id;
	$.ajax({
		type: 'delete',
		url: 'note/' + id + '/',
		success: refresh,
		error: fail
	});
}

function refresh() {
	$.ajax({
		url: 'notes/',
		dataType: 'json',
		success: show,
		error: fail
	});
}

function fail(request, status, error) {
	var dialog = $('<div title="' + status + '"></div>').html(error || 'Could not communicate with server.');
	$(dialog).dialog({modal: true, resizable: false});
}

$(function() {
	$('#new').stickynote({
		containment: 'content',
		size: 'small',
		ontop: true,
		oncreate: create,
		onstop: move,
		x: 50
	});

	$('#refresh').click(refresh);
	
	refresh();
});
