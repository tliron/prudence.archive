;
; Prudence Hosts
;
; Prudence supports virtual hosting, allowing you to serve different applications
; or otherwise route differently per domain name, protocol, port, etc. This
; feature lets you run multiple sites from the same Prudence installation.
;
; Note that virtual hosts are only indirectly related to Prudence's servers.
; See servers.clj for more information.
;

(import 'org.restlet.routing.VirtualHost)

;
; All
;
; Our "all" host will accept all incoming requests.
;

(def all-host (VirtualHost. (.getContext component)))
(.setName all-host "all")

(.. component getHosts (add all-host))

;
; mysite.org
;
; This is an example of a virtual host which only accepts requests to
; a specific set of domains.
;

(def mysite-host (VirtualHost. (.getContext component)))
(.setName mysite-host "mysite.org")
(.setHostScheme mysite-host "http")
(.setHostDomain mysite-host "mysite.org|www.mysite.org")
(.setHostPort mysite-host "80")

(.. component getHosts (add mysite-host))

;
; Default Host
;
; Applications by default will attach only to this host, though they can
; choose to attach to any hosts defined here. See the application's
; routing.clj.
;

(.setDefaultHost component all-host)
