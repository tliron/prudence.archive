
document.container.include('defaults/application/routing');

router.attach(fixURL(resourceBaseURL + '/note/{id}'), new Renamer(application.context, resources, fixURL(resourceBaseURL + '/note')));
