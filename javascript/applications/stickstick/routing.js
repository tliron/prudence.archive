//
// Stickstick Routing
//

document.container.include('defaults/application/routing');

router.redirectRelative(fixURL(resourcesBaseURL + '/data/note/{id}/'), '..');
//router.capture(fixURL(resourcesBaseURL + '/note/{id}/'), '/note');
//router.captureOther(fixURL(resourcesBaseURL + '/note/{id}/'), 'stickstick', '/note');
