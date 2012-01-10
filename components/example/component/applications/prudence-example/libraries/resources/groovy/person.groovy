
//java.lang.System.out.println('ZXXXXXXXXXXXXXXXXXXXXXXXX')

class Person {
	def handleInit(conversation) {
		conversation.addMediaTypeByName('text/html')
		conversation.addMediaTypeByName('text/plain')
	}

	def handleGet(conversation) {
		def id = conversation.locals['id']
		return "I am person ${id}, formatted as \"${conversation.mediaTypeName}\", encased in Groovy"
	}
}
