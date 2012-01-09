
document.executeOnce('/sincerity/classes/')
document.executeOnce('/sincerity/templates/')

Person = Sincerity.Classes.define(function() {
	var Public = {}
	
	Public.handleInit = function(conversation) {
		conversation.addMediaTypeByName('text/html')
		conversation.addMediaTypeByName('text/plain')
	}

	Public.handleGet = function(conversation) {
		var id = conversation.locals.get('id')
		if (id == 13) {
			// This will generate an error!
			// (We're doing it on purpose in order to see the debug page)
			abc()
		}
		return 'I am person {0}, formatted as "{1}", encased in JavaScript'.cast(id, conversation.mediaTypeName)
	}

	return Public
}())