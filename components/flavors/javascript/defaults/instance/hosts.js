//
// Prudence Hosts
//
// Copyright 2009-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

//
// Prudence supports virtual hosting, allowing you to serve different applications
// or otherwise route differently per domain name, protocol, port, etc. This
// feature lets you run multiple sites from the same Prudence installation.
//
// Note that virtual hosts are only indirectly related to Prudence's servers.
// See servers.js for more information.
//

importClass(
	org.restlet.routing.VirtualHost)

//
// All
//
// Our "all" host will accept all incoming requests.
//

var allHost = new VirtualHost(component.context)
allHost.name = 'all hosts'

component.hosts.add(allHost)

//
// mysite.org
//
// This is an example of a virtual host which only accepts requests to
// a specific set of domains.
//

var mysiteHost = new VirtualHost(component.context)
mysiteHost.name = 'mysite.org'
mysiteHost.hostScheme = 'http'
mysiteHost.hostDomain = 'mysite.org|www.mysite.org'
mysiteHost.hostPort = '80'

component.hosts.add(mysiteHost)

//
// Default Host
//
// Applications by default will attach only to this host, though they can
// choose to attach to any hosts defined here. See the application's
// routing.js.
//

component.defaultHost = allHost
