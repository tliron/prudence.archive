#
# Prudence Application Routing
#
# Note that order of attachment is important: first matching pattern wins.
#

from java.lang import ClassLoader
from java.io import File
from org.restlet.routing import Router, Redirector, Template
from org.restlet.resource import Directory
from com.threecrickets.prudence.util import FallbackRouter

classLoader = ClassLoader.getSystemClassLoader()

#
# Utilities
#

# Makes sure we have slashes where we expect them
def fix_url(url):
	if len(url) > 0 and url[0] == '/':
		url = url[1:]
	if len(url) > 0 and url[-1] != '/':
		url = url + '/'
	return url

# Moves a route to be the one before the last
def penultimate_route(route):
	router.routes.remove(route)
	router.routes.add(router.routes.size() - 1, route)
	return route

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See component/hosts.js for more information.
#

redirector = Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER)

sys.stdout.write('%s: ' % application.name)
for i in range(len(hosts)):
	host, url = hosts.items()[i]
	if url is None:
		url = application_default_url
	sys.stdout.write('"%s" on %s' % (url, host.name))
	host.attach(url, application).matchingMode = Template.MODE_STARTS_WITH
	if url != '/':
		if url[-1] == '/':
			url = url[:-1]
		host.attach(url, redirector)
	if i < len(hosts) - 1:
		sys.stdout.write(', ')
print '.'

#
# Inbound root
#

router = FallbackRouter(application.context)
router.routingMode = Router.MODE_BEST_MATCH
application.inboundRoot = router

#
# Add trailing slashes
#

if len(url_add_trailing_slash) > 0:
	for url in url_add_trailing_slash:
		url = fix_url(url)
		if len(url) > 0:
			if url[-1] == '/':
				url = url[:-1]
			router.attach(url, redirector)

#
# Dynamic web
#

router.attach(fix_url(dynamic_web_base_url), classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH

#
# Static web
#

static_web = Directory(application.context, File(application_base_path + static_web_base_path).toURI().toString())
static_web.listingAllowed = static_web_directory_listing_allowed
static_web.negotiateContent = True

router.attach(fix_url(static_web_base_url), static_web).matchingMode = Template.MODE_STARTS_WITH

#
# Resources
#

router.attach(fix_url(resource_base_url), classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH
