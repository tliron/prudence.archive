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

def applicationURL(url):
	return (applicationBaseURL + url).replace('//', '/')

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See start/hosts.js for more information.
#

sys.stdout.write('Attached application "%s" to "%s" on virtual hosts ' % (application.name, applicationBaseURL))
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

if len(urlAddTrailingSlash) > 0:
	redirector = Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER)
	for url in urlAddTrailingSlash:
		url = applicationURL(url)
		if url[-1] == '/':
			url = url[0:-1]
		if len(url) > 0:
			router.attach(url, redirector)

#
# Static web
#

staticWeb = Directory(application.context, File(applicationBasePath + staticWebBasePath).toURI().toString())
staticWeb.listingAllowed = staticWebDirectoryListingAllowed
staticWeb.negotiateContent = True

router.attach(applicationURL(staticWebBaseURL), staticWeb).matchingMode = Template.MODE_STARTS_WITH

#
# Resources
#

router.attach(applicationURL(resourceBaseURL), classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH

#
# Dynamic web
#

router.attach(applicationURL(dynamicWebBaseURL), classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH
