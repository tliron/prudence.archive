;
; Stickstick Routing
;

(.. document getContainer (include "defaults/application/routing"))

(.rewrite router (fix-url (str resources-base-url "/note/{id}/")) "{ri}..")
