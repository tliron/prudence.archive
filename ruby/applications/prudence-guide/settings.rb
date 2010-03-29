#
# Prudence Guide Settings
#

@executable.container.include 'defaults/application/settings'

$application_name = 'Prudence Guide'
$application_description = 'Prudence web site, documentation, and tests'
$application_author = 'Tal Liron'
$application_owner = 'Three Crickets'
$application_home_url = 'http://threecrickets.com/prudence/'
$application_contact_email = 'prudence@threecrickets.com'

$hosts = [[$component.default_host, nil], [$mysite_host, nil]]

$show_debug_on_error = true

$preheat_resources = ['/data/jython/', '/data/jruby/', '/data/groovy/', '/data/clojure/', '/data/rhino/']
