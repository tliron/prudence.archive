
function handleInit(conversation) {}
​
function handleGet(conversation) {
	application.task(null, '/tasks/ask-whats-up/', 'ask', null, 0, 0, false)
	
	// Redirect to where we came from
	conversation.response.redirectSeeOther(conversation.request.referrerRef)
	return null
}
