//
// Stickstick Routing
//

document.container.include('defaults/application/routing');

router.attach(fixURL(resourcesBaseURL + '/note/{id}'), new Redirector(application.context, '{fi}note', Redirector.MODE_SERVER_DISPATCHER));
