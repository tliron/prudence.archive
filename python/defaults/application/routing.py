#
# Prudence Application Routing
#

import sys

from java.lang import ClassLoader
from java.io import File

from org.restlet.routing import Router, Redirector, Template
from org.restlet.resource import Finder, Directory
from com.threecrickets.scripturian.util import DefrostTask
from com.threecrickets.scripturian.file import DocumentFileSource
from com.threecrickets.prudence.util import PrudenceRouter, PreheatTask

class_loader = ClassLoader.getSystemClassLoader()

#
# Utilities
#

# Makes sure we have slashes where we expect them
def fix_url(url):
	url = url.replace('//', '/') # no doubles
	if len(url) > 0 and url[0] == '/': # never at the beginning
		url = url[1:]
	if len(url) > 0 and url[-1] != '/': # always at the end
		url = url + '/'
	return url

#
# Internal router
#

component.internalRouter.attach('/%s/' % application_internal_name, application).matchingMode = Template.MODE_STARTS_WITH

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See component/hosts.py for more information.
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
		host.attach(url, add_trailing_slash).matchingMode = Template.MODE_EQUALS
	if i < len(hosts) - 1:
		sys.stdout.write(', ')
print '.'

attributes = application.context.attributes

attributes['component'] = component

#
# Inbound root
#

router = PrudenceRouter(application.context)
router.routingMode = Router.MODE_BEST_MATCH
application.inboundRoot = router

#
# Add trailing slashes
#

for url in url_add_trailing_slash:
	url = fix_url(url)
	if len(url) > 0:
		if url[-1] == '/':
			url = url[:-1]
		router.attach(url, add_trailing_slash)

#
# Dynamic web
#

language_manager = executable.context.manager
dynamic_web_document_source = DocumentFileSource(application_base_path + dynamic_web_base_path, dynamic_web_default_document, dynamic_web_minimum_time_between_validity_checks)
attributes['com.threecrickets.prudence.GeneratedTextResource.languageManager'] = language_manager
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag'] = 'python'
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = dynamic_web_default_document
attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = dynamic_web_document_source
attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = dynamic_web_source_viewable

dynamic_web = Finder(application.context, class_loader.loadClass('com.threecrickets.prudence.GeneratedTextResource'))
router.attachBase(fix_url(dynamic_web_base_url), dynamic_web)

if dynamic_web_defrost:
	for defrost_task in DefrostTask.forDocumentSource(dynamic_web_document_source, language_manager, True, True):
		tasks.append(defrost_task)

#
# Static web
#

static_web = Directory(application.context, File(application_base_path + static_web_base_path).toURI().toString())
static_web.listingAllowed = static_web_directory_listing_allowed
static_web.negotiateContent = True
router.attachBase(fix_url(static_web_base_url), static_web)

#
# Resources
#

resources_document_source = DocumentFileSource(application_base_path + resources_base_path, resources_default_name, resources_minimum_time_between_validity_checks)
attributes['com.threecrickets.prudence.DelegatedResource.languageManager'] = language_manager
attributes['com.threecrickets.prudence.DelegatedResource.defaultLanguageTag'] = 'python'
attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = resources_default_name
attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = resources_document_source
attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = resources_source_viewable

resources = Finder(application.context, class_loader.loadClass('com.threecrickets.prudence.DelegatedResource'))
router.attachBase(fix_url(resources_base_url), resources)

if resources_defrost:
	for defrost_task in DefrostTask.forDocumentSource(resources_document_source, language_manager, False, True):
		tasks.append(defrost_task)

#
# SourceCode
#

if show_debug_on_error:
	attributes['com.threecrickets.prudence.SourceCodeResource.documentSources'] = [dynamic_web_document_source, resources_document_source]
	source_code = Finder(application.context, class_loader.loadClass('com.threecrickets.prudence.SourceCodeResource'))
	router.attach(fix_url(show_source_code_url), source_code).matchingMode = Template.MODE_EQUALS

#
# Preheat
#

if dynamic_web_preheat:
	for preheat_task in PreheatTask.forDocumentSource(dynamic_web_document_source, component.context, application_internal_name):
		tasks.append(preheat_task)

for preheat_resource in preheat_resources:
	tasks.append(PreheatTask(component.context, application_internal_name, preheat_resource))
