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

import org.restlet.data.Protocol

// Required for use of Directory
clientFile = componentInstance.clients.add(Protocol.FILE)

// Required for accessing external resources
clientHttp = componentInstance.clients.add(Protocol.HTTP)
clientHttp.connectTimeout = 10000
clientHttp.context.parameters.set('socketTimeout', '10000')

// Required for accessing external resources
clientHttps = componentInstance.clients.add(Protocol.HTTPS)
clientHttps.connectTimeout = 10000
clientHttps.context.parameters.set('socketTimeout', '10000')
