
document.container.include('defaults/application/routing');

router.attach(fixURL(resourceBaseURL + '/note/{id}'), new Redirector(application.context, '{oi}/note', Redirector.MODE_SERVER_DISPATCHER));
