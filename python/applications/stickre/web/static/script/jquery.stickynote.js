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
		size 	: 'medium',
		event	: 'click',
		color	: '#000000',
		x		: 0,
		y		: 0
	};
	
	$.fn.stickynote.createNote = function(o) {
		var _note_content = $(document.createElement('textarea'));
		var _div_note 	= 	$(document.createElement('div'))
							.addClass('jStickyNote')
							.css('cursor','move');
		if(!o.content) {
			_div_note.append(_note_content);
			var _div_create = $(document.createElement('div'))
						.addClass('jSticky-create')
						.attr('title','Create Sticky Note');
		
			_div_create.click(function(e) {
				var textarea = $(this)
								.parent()
								.find('textarea');
				var text = textarea.val();
				
				var _p_note_text = 	$(document.createElement('p'))
									.css('color', o.color)
									.html(text);
				
				textarea
				.before(_p_note_text)
				.remove();
				
				$(this).remove();
				
				if(o.oncreate) {
					o.oncreate(this, text);
				}
			})
		}	
		else
			_div_note.append('<p style="color:'+o.color+'">'+o.content+'</p>');					
		
		var _div_delete = 	$(document.createElement('div'))
							.addClass('jSticky-delete');
		
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
		
		var _div_wrap 	= 	$(document.createElement('div'))
							.addClass('jSticky')
							.css({'position': 'relative', 'top': o.y, 'left': o.x})
							.append(_div_note)
							.append(_div_delete)
							.append(_div_create);
		
		switch(o.size) {
			case 'large':
				_div_wrap.addClass('jSticky-large');
				break;
			case 'medium':
				_div_wrap.addClass('jSticky-medium');
				break;
		}
		
		if(o.containment) {
			_div_wrap.draggable({containment: '#'+o.containment, scroll: false, start: function(event, ui) {
				if(o.ontop)
					$(this).parent().append($(this));
			}});	
			$('#'+o.containment).append(_div_wrap);
		}
		else{
			_div_wrap.draggable({scroll: false, start: function(event, ui) {
				if(o.ontop)
					$(this).parent().append($(this));
			}});
			$('body').append(_div_wrap);
		}
		
	};
})(jQuery);