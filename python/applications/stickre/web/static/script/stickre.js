
function fail(request, status, error) {
	var dialog = $('<div title="' + status + '"></div').html(error || 'Could not communicate with server.');
	$(dialog).dialog({modal: true, resizable: false});
}

function create(div, text) {
	var pos = $(div).position();
	text = text.replace('\n', '<br />');
	$.ajax({
		type: 'put',
		url: 'notes/',
		dataType: 'json',
		contentType: 'application/json',
		data: JSON.stringify({content: text, x: pos.left, y: pos.top}),
		success: show,
		error: fail
	});
}

function destroy(div, obj) {
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

function show(notes) {
	clear();
	for(var i in notes) {
		notes[i].containment = 'content';
		notes[i].size = 'medium';
		notes[i].ontop = true;
		notes[i].ondelete = destroy;
		$('#content').stickynote.createNote(notes[i]);
	}
}

function clear() {
	$('div.jSticky').remove();
}

$(function() {
	$('#new').stickynote({
		containment: 'content',
		size: 'medium',
		ontop: true,
		oncreate: create
	});

	$('#refresh').click(refresh);
	
	refresh();
});
