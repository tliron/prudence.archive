#
# Prudence Admin Settings
#

$document.execute 'defaults/application/settings/'

$application_name = 'Prudence Admin'
$application_description = 'Runtime management of Prudence'
$application_author = 'Tal Liron'
$application_owner = 'Three Crickets'
$application_home_url = 'http://threecrickets.com/prudence/'
$application_contact_email = 'prudence@threecrickets.com'

$hosts = [[$component.default_host, '/'], [$mysite_host, '/']]

$show_debug_on_error = true