//
// Prudence Clients
//

importClass(
	org.restlet.data.Protocol);

// Required for use of Directory
component.clients.add(Protocol.FILE);

// Required for accessing external resources
component.clients.add(Protocol.HTTP);
