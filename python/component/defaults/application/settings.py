#
# Prudence Application Settings
#

#application_name = 'Prudence Application' # Defaults to the application directory name
application_description = 'This is a Prudence application.'
application_author = 'Anonymous'
application_owner = 'Public Domain'
application_home_url = 'http://www.threecrickets.com/prudence/'
application_contact_email = 'prudence@threecrickets.com'
#application_logger_name = 'prudence-application' # Defaults to the application directory name

#
# Base URL
#
# All URLs will be under this. Defaults to the directory name under /applications,
# though you can override it here. For example, if you want your application to available
# at the root URL, set it to "/".
#

#application_base_url = '/'

#
# Hosts
#
# These are the virtual hosts to which our application will be attached.
# See component/hosts.py for more information.
#

hosts = [component.defaultHost]

#
# Resources
#
# Sets up a directory under which you can place script files that implement
# RESTful resources. The directory structure underneath the base directory
# is directly linked to the base URL.
#

resource_base_url = '/resource/'
resource_base_path = '/resources/'

# If the URL points to a directory rather than a file, and that directory
# contains a file with this name, then it will be used. This allows
# you to use the directory structure to create nice URLs without relying
# on filenames.

resource_default_name = 'default'

# This is so we can see the source code for scripts by adding ?source=true
# to the URL. You probably wouldn't want this for most applications.

resource_source_viewable = True

# This is the time (in milliseconds) allowed to pass until a script file
# is tested to see if it was changed. During development, you'd want this
# to be low, but during production, it should be high in order to avoid
# unnecessary hits on the filesystem.

resource_minimum_time_between_validity_checks = 1000

#
# Dynamic Web
#
# Sets up a directory under which you can place text files that support embedded scriptlets.
# Note that the generated result can be cached for better performance.
#

dynamic_web_base_url = '/'
dynamic_web_base_path = '/web/'

# If the URL points to a directory rather than a file, and that directory
# contains a file with this name, then it will be used. This allows
# you to use the directory structure to create nice URLs that do not
# contain filenames.

dynamic_web_default_document = 'index'

# This is so we can see the source code for scripts by adding ?source=true
# to the URL. You probably wouldn't want this for most applications.

dynamic_web_source_viewable = True

# This is the time (in milliseconds) allowed to pass until a script file
# is tested to see if it was changed. During development, you'd want this
# to be low, but during production, it should be high in order to avoid
# unnecessary hits on the filesystem.

dynamic_web_minimum_time_between_validity_checks = 1000

#
# Static Web
#
# Sets up a directory under which you can place static files of any type.
# Servers like Grizzly and Jetty can use non-blocking I/O to stream static
# files efficiently to clients. 
#

static_web_base_url = '/static/'
static_web_base_path = '/web/static/'

# If the URL points to a directory rather than a file, then this will allow
# automatic creation of an HTML page with a directory listing.

static_web_directory_listing_allowed = True

#
# URL manipulation
#

# The URLs in this array will automatically be redirected to have a trailing
# slash added to them if it's missing.

url_add_trailing_slash = ['', dynamic_web_base_url, static_web_base_url]
