#
# Stickstick Settings
#

$document.execute 'defaults/application/settings/'

$application_name = 'Stickstick'
$application_description = 'Share online sticky notes'
$application_author = 'Tal Liron'
$application_owner = 'Three Crickets'
$application_home_url = 'http://threecrickets.com/prudence/stickstick/'
$application_contact_email = 'prudence@threecrickets.com'

$predefined_globals['stickstick.backend'] = 'h2'
$predefined_globals['stickstick.username'] = 'root'
$predefined_globals['stickstick.password'] = 'root'
$predefined_globals['stickstick.host'] = ''
$predefined_globals['stickstick.database'] = document.source.base_path.path + '/data/stickstick/stickstick'

$show_debug_on_error = true

$preheat_resources = ['data/']
