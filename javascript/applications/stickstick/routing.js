//
// Stickstick Routing
//

document.container.include('defaults/application/routing');

router.capture(fixURL(resourcesBaseURL + '/data/note/{id}/'), '/note');
