
document.container.include('defaults/application/routing')

router.attach(fix_url(resource_base_url + '/note/{id}'), Renamer(application.context, resources, fix_url(resource_base_url + '/note')))
