#
# Prudence Application Routing
#

from java.lang import ClassLoader
from java.io import File
from javax.script import ScriptEngineManager

from org.restlet.routing import Router, Redirector, Template
from org.restlet.resource import Finder, Directory
from com.threecrickets.scripturian import Defroster
from com.threecrickets.scripturian.file import DocumentFileSource
from com.threecrickets.prudence.util import FallbackRouter

classLoader = ClassLoader.getSystemClassLoader()

#
# Utilities
#

# Makes sure we have slashes where we expect them
def fix_url(url):
	url = url.replace('//', '/')
	if len(url) > 0 and url[0] == '/':
		url = url[1:]
	if len(url) > 0 and url[-1] != '/':
		url = url + '/'
	return url

#
# Internal Router
#

component.internalRouter.attach('/' + application_internal_name, application)

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See component/hosts.js for more information.
#

add_trailing_slash = Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT)

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
		host.attach(url, add_trailing_slash)
	if i < len(hosts) - 1:
		sys.stdout.write(', ')
print '.'

attributes = application.context.attributes

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
			router.attach(url, add_trailing_slash)

#
# Dynamic web
#

dynamic_web = Finder(application.context, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource'))

script_engine_manager = ScriptEngineManager()
document_source = DocumentFileSource(application_base_path + dynamic_web_base_path, dynamic_web_default_document, dynamic_web_minimum_time_between_validity_checks)

attributes['com.threecrickets.prudence.GeneratedTextResource.engineManager'] = script_engine_manager
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultEngineName'] = 'python'
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = dynamic_web_default_document
attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = document_source
attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = dynamic_web_source_viewable

router.attach(fix_url(dynamic_web_base_url), dynamic_web).matchingMode = Template.MODE_STARTS_WITH

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

resources = Finder(application.context, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource'))

document_source = DocumentFileSource(application_base_path + resource_base_path, resource_default_name, resource_minimum_time_between_validity_checks)
attributes['com.threecrickets.prudence.DelegatedResource.engineManager'] = script_engine_manager
attributes['com.threecrickets.prudence.DelegatedResource.defaultEngineName'] = 'python'
attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = resource_default_name
attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = document_source
attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = resource_source_viewable

router.attach(fix_url(resource_base_url), resources).matchingMode = Template.MODE_STARTS_WITH
