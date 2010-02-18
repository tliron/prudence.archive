;
; Stickstick Routing
;

(.. document getContainer (include "defaults/application/routing"))

(.attach router (fix-url (str resources-base-url "/note/{id}")) (Redirector. (.getContext application) "{fi}note" Redirector/MODE_SERVER_DISPATCHER))
