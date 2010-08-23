;
; Prudence Clients
;

(import 'org.restlet.data.Protocol)

; Required for use of Directory
(.. component getClients (add Protocol/FILE))

; Required for accessing external resources
(.. component getClients (add Protocol/HTTP))
(.. component getClients (add Protocol/HTTPS))
