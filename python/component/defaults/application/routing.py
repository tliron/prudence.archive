#
# Prudence Application Routing
#
# Note that order of attachment is important: first matching pattern wins.
#

from java.lang import ClassLoader
from java.io import File
from org.restlet.routing import Router, Redirector, Template
from org.restlet.resource import Directory

classLoader = ClassLoader.getSystemClassLoader()

#
# Utilities
#

# Creates a URL relative to the application_base_url 
def application_url(url):
	return (application_base_url + url).replace('//', '/')

# Moves a route to be the one before the last
def penultimate_route(route):
	router.routes.remove(route)
	router.routes.add(router.routes.size() - 1, route)

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See start/hosts.js for more information.
#

sys.stdout.write('Attached application "%s" to "%s" on virtual hosts ' % (application.name, application_base_url))
for i in range(len(hosts)):
	host = hosts[i]
	host.attach(application)
	sys.stdout.write('"%s"' % host.name)
	if i < len(hosts) - 1:
		sys.stdout.write(', ')
print '.'

#
# Inbound root
#

router = Router(application.context)
application.inboundRoot = router

#
# Add trailing slashes
#

if len(url_add_trailing_slash) > 0:
	redirector = Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER)
	for url in url_add_trailing_slash:
		url = application_url(url)
		if url[-1] == '/':
			url = url[0:-1]
		if len(url) > 0:
			router.attach(url, redirector)

#
# Static web
#

static_web = Directory(application.context, File(application_base_path + static_web_base_path).toURI().toString())
static_web.listingAllowed = static_web_directory_listing_allowed
static_web.negotiateContent = True

router.attach(application_url(static_web_base_url), static_web).matchingMode = Template.MODE_STARTS_WITH

#
# Resources
#

router.attach(application_url(resource_base_url), classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH

#
# Dynamic web
#

router.attach(application_url(dynamic_web_base_url), classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH
