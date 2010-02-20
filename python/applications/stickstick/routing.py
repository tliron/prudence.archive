
document.container.include('defaults/application/routing')

router.rewrite(fix_url(resources_base_url + '/note/{id}/'), '{ri}..')
