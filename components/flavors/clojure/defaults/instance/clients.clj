;
; Prudence Clients
;
; Copyright 2009-2011 Three Crickets LLC.
;
; The contents of this file are subject to the terms of the LGPL version 3.0:
; http://www.gnu.org/copyleft/lesser.html
;
; Alternatively, you can obtain a royalty free commercial license with less
; limitations, transferable or non-transferable, directly from Three Crickets
; at http://threecrickets.com/
;

(import 'org.restlet.data.Protocol)

; Required for use of Directory
(def client-file (.. component getClients (add Protocol/FILE)))

; Required for accessing external resources
(def client-http (.. component getClients (add Protocol/HTTP)))
(.setConnectTimeout client-http 10000)
(.set (.getParameters (.getContext client-http)) "socketTimeout" "10000")

; Required for accessing external resources
(def client-https (.. component getClients (add Protocol/HTTPS)))
(.setConnectTimeout client-https 10000)
(.set (.getParameters (.getContext client-https)) "socketTimeout" "10000")
