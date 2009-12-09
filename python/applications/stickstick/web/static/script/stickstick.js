
function clear() {
	$('div.stickynote').remove();
}

function show(notes) {
	clear();
	for(var i in notes) {
		$('#content').stickynote.create({
			containment: 'content',
			x: notes[i].x,
			y: notes[i].y,
			size: notes[i].size == 2 ? 'large' : 'small',
			content: notes[i].content,
			ontop: true,
			ondelete: destroy,
			onstop: move
		})
		.data('stickstick.id', notes[i].id)
		.data('stickstick.board', notes[i].board);
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
		data: JSON.stringify({
			board: '',
			x: pos.left - ppos.left,
			y: pos.top - ppos.top,
			size: note.hasClass('stickynote-large') ? 2 : 1,
			content: text
		}),
		success: show,
		error: fail
	});
}

function move(event, note) {
	var id = note.helper.data('stickstick.id');
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

function destroy(note) {
	var id = $(note).parent().data('stickstick.id');
	if(id) {
		$.ajax({
			type: 'delete',
			url: 'note/' + id + '/',
			success: refresh,
			error: fail
		});
	}
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
	$('#new-small').stickynote({
		containment: 'content',
		size: 'small',
		ontop: true,
		oncreate: create,
		onstop: move,
		x: 50
	});

	$('#new-large').stickynote({
		containment: 'content',
		size: 'large',
		ontop: true,
		oncreate: create,
		onstop: move,
		x: 50
	});

	$('#refresh').click(refresh);
	
	refresh();
});
