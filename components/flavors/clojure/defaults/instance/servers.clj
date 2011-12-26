;
; Prudence Servers
;
; Copyright 2009-2011 Three Crickets LLC.
;
; The contents of this file are subject to the terms of the LGPL version 3.0:
; http://www.opensource.org/licenses/lgpl-3.0.html
;
; Alternatively, you can obtain a royalty free commercial license with less
; limitations, transferable or non-transferable, directly from Three Crickets
; at http://threecrickets.com/
;

;
; Handles communication with clients.
;
; Often one server is enough, but Prudence supports multiple servers, so that you can
; handle requests coming through various ports and protocols, or to bind to different
; IP addresses (representing different network interfaces, VPNs, etc.) on your machine.
;
; A server can be set up to run behind another web server via a proxy.
; For Apache, this requires mod_proxy.
;
; Note that servers don't handle the actual routing. Your resources are instead attached
; to virtual hosts. See hosts.js for more information.
;

(import
	'org.restlet.Server
	'org.restlet.data.Protocol)

;
; Default HTTP server
;
; Binds to the machine's default IP address.
;

(def default-server (Server. Protocol/HTTP 8080))
(.setName default-server "default")
(.. component getServers (add default-server))

; Add support for the X-FORWARDED-FOR header used by proxies, such as Apache's
; mod_proxy. This guarantees that request.clientInfo.upstreamAddress returns
; the upstream address behind the proxy.
(.. default-server getContext getParameters (add "useForwardedForHeader" "true"))

;
; HTTP server bound to a specific IP address
;
; This is an example of binding a server to an IP address representing one of
; several of the machine's network interfaces. In this case, let's pretend
; that it's the interface open to the Internet at large.
;

;(def world-server (Server. Protocol/HTTP "192.168.1.2" 80))
;(.setName world-server "world")
;(.. component getServers (add "world-server"))
