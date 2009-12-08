/*

This source code, and its related CSS and images, were modified
by Tal Liron based on the work of Paul Bakaus.

Below is the original license (MIT).

---

Copyright (c) 2009 Paul Bakaus, http://jqueryui.com/

This software consists of voluntary contributions made by many
individuals (AUTHORS.txt, http://jqueryui.com/about) For exact
contribution history, see the revision history and logs, available
at http://jquery-ui.googlecode.com/svn/

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

(function($) {
	
	$.fn.stickynote = function(options) {
		var opts = $.extend({}, $.fn.stickynote.defaults, options);
		return this.each(function() {
			$this = $(this);
			var o = $.meta ? $.extend({}, opts, $this.data()) : opts;
			switch(o.event) {
				case 'dblclick':
					$this.dblclick(function(e) {$.fn.stickynote.createNote(o);})
					break;
				case 'click':
					$this.click(function(e) {$.fn.stickynote.createNote(o);})
					break;
			}		
		});
	};
	
	$.fn.stickynote.defaults = {
		size 	: 'small',
		event	: 'click',
		color	: '#000000',
		x		: 0,
		y		: 0
	};
	
	$.fn.stickynote.createNote = function(o) {
		var _div_wrap = $(document.createElement('div'))
			.attr('noteid', o.id ? o.id : '')
			.addClass('stickynote')
			.addClass('stickynote-' + o.size)
			.css({'position': 'absolute', 'left': o.x, 'top': o.y});

		var _div_note = $(document.createElement('div'))
			.addClass('stickynote-note')
			.css('cursor','move');
		_div_wrap.append(_div_note);
		
		if(!o.content) {
			var _note_content = $(document.createElement('textarea'));
		
			_div_note.append(_note_content);

			var _div_create = $(document.createElement('div'))
				.addClass('stickynote-create')
				.attr('title','Create Sticky Note');
			_div_wrap.append(_div_create);

			_div_create.click(function(e) {
				var wrapper = $(this).parent();
				var textarea = wrapper.find('textarea');
				var text = textarea.val();
				
				var _p_note_text = 	$(document.createElement('p'))
					.css('color', o.color)
					.html(text);
				
				textarea
					.before(_p_note_text)
					.remove();
				
				$(this).remove();
				
				if(o.oncreate) {
					o.oncreate(wrapper, text);
				}
			});
		}	
		else {
			_div_note.append('<p style="color:'+o.color+'">'+o.content+'</p>');
		}
		
		var _div_delete = $(document.createElement('div'))
			.addClass('stickynote-delete');
		_div_wrap.append(_div_delete);
		
		if(!o.ondelete) {
			_div_delete.click(function(e) {
				$(this).parent().remove();
			});
		}
		else {
			_div_delete.click(function(e) {
				o.ondelete(this, o);
			});
		}
		
		function onstart(event, ui) {
			if(o.ontop)
				$(ui).parent().append($(ui));
		}
		
		if(o.containment) {
			_div_wrap.draggable({containment: '#' + o.containment, scroll: false, start: onstart, stop: o.onstop});	
			$('#' + o.containment).append(_div_wrap);
		}
		else {
			_div_wrap.draggable({scroll: false, start: onstart, stop: o.onstop});
			$('body').append(_div_wrap);
		}
	};
})(jQuery);