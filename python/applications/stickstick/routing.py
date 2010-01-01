
document.container.include('defaults/application/routing')

router.attach(fix_url(resource_base_url + '/note/{id}'), Redirector(application.context, '{oi}/note', Redirector.MODE_SERVER_DISPATCHER))
