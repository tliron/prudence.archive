;
; Stickstick Routing
;

(.. document getContainer (include "defaults/application/routing"))

(.capture router (fix-url (str resources-base-url "/data/note/{id}/")) "/data/note/")
