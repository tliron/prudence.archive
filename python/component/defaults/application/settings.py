#
# Prudence Application Settings
#

#applicationName = 'Prudence Application'; # Defaults to the application directory name
applicationDescription = 'This is a Prudence application.'
applicationAuthor = 'Anonymous'
applicationOwner = 'Public Domain'
applicationHomeURL = 'http://www.threecrickets.com/prudence/'
applicationContactEmail = 'prudence@threecrickets.com'
applicationLoggerName = 'prudence-application'

#
# Base URL
#
# All URLs will be under this. Defaults to the directory name under /applications,
# though you can override it here. For example, if you want your application to available
# at the root URL, set it to "/".
#

#applicationBaseURL = '/'

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

resourceBaseURL = '/resource/'
resourceBasePath = '/resources/'

# If the URL points to a directory rather than a file, and that directory
# contains a file with this name, then it will be used. This allows
# you to use the directory structure to create nice URLs without relying
# on filenames.

resourceDefaultName = 'default'

# This is so we can see the source code for scripts by adding ?source=true
# to the URL. You probably wouldn't want this for most applications.

resourceSourceViewable = True

# This is the time (in milliseconds) allowed to pass until a script file
# is tested to see if it was changed. During development, you'd want this
# to be low, but during production, it should be high in order to avoid
# unnecessary hits on the filesystem.

resourceMinimumTimeBetweenValidityChecks = 1000

#
# Dynamic Web
#
# Sets up a directory under which you can place text files that support embedded scriptlets.
# Note that the generated result can be cached for better performance.
#

dynamicWebBaseURL = '/'
dynamicWebBasePath = '/web/'

# If the URL points to a directory rather than a file, and that directory
# contains a file with this name, then it will be used. This allows
# you to use the directory structure to create nice URLs that do not
# contain filenames.

dynamicWebDefaultDocument = 'index'

# This is so we can see the source code for scripts by adding ?source=true
# to the URL. You probably wouldn't want this for most applications.

dynamicWebSourceViewable = True

# This is the time (in milliseconds) allowed to pass until a script file
# is tested to see if it was changed. During development, you'd want this
# to be low, but during production, it should be high in order to avoid
# unnecessary hits on the filesystem.

dynamicWebMinimumTimeBetweenValidityChecks = 1000

#
# Static Web
#
# Sets up a directory under which you can place static files of any type.
# Servers like Grizzly and Jetty can use non-blocking I/O to stream static
# files efficiently to clients. 
#

staticWebBaseURL = '/static/'
staticWebBasePath = '/web/static/'

# If the URL points to a directory rather than a file, then this will allow
# automatic creation of an HTML page with a directory listing.

staticWebDirectoryListingAllowed = True

#
# URL manipulation
#

# The URLs in this array will automatically be redirected to have a trailing
# slash added to them if it's missing.

urlAddTrailingSlash = ['', dynamicWebURL, staticWebBaseURL]
