//
// Prudence Clients
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

importClass(
	org.restlet.data.Protocol)

// Required for use of Directory
var clientFile = component.clients.add(Protocol.FILE)

// Required for accessing external resources
var clientHttp = component.clients.add(Protocol.HTTP)
clientHttp.context.parameters.set('socketTimeout', 10000)
var clientHttps = component.clients.add(Protocol.HTTPS)
clientHttps.context.parameters.set('socketTimeout', 10000)
