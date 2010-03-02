
document.container.include('defaults/application/routing')

router.capture(fix_url(resources_base_url + '/note/{id}/'), '/note')
