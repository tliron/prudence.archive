
importClass(
	org.restlet.data.Protocol)

// Required for accessing external resources
var client = component.clients.add(Protocol.HTTP)
client.connectTimeout = 10000
client.context.parameters.set('socketTimeout', 10000)
