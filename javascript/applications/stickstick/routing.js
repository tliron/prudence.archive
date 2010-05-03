//
// Stickstick Routing
//

executable.container.execute('defaults/application/routing/');

router.capture(fixURL(resourcesBaseURL + '/data/note/{id}/'), '/data/note/');
