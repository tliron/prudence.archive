//
// Stickstick Routing
//

document.container.include('defaults/application/routing');

router.rewrite(fixURL(resourcesBaseURL + '/note/{id}/'), '{ri}..');
