#
# Prudence Application Routing
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.gnu.org/copyleft/lesser.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

import sys

from java.lang import ClassLoader
from java.io import File
from java.util.concurrent import ConcurrentHashMap, CopyOnWriteArrayList, CopyOnWriteArraySet

from org.restlet.routing import Router, Redirector, Template
from org.restlet.resource import Finder, Directory
from org.restlet.engine.application import Encoder
from com.threecrickets.scripturian.util import DefrostTask
from com.threecrickets.scripturian.document import DocumentFileSource
from com.threecrickets.prudence import PrudenceRouter
from com.threecrickets.prudence.util import Fallback, PreheatTask, PhpExecutionController

class_loader = ClassLoader.getSystemClassLoader()

#
# Utilities
#

# Makes sure we have slashes where we expect them
def fix_url(url):
	url = url.replace('//', '/') # no doubles
	if url == '' or url[0] != '/': # always at the beginning
		url = '/' + url
	if url != '/' and url[-1] != '/': # always at the end
		url = url + '/'
	return url

#
# Internal router
#

component.internalRouter.attach('/' + application_internal_name, application_instance).matchingMode = Template.MODE_STARTS_WITH

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See defaults/instance/hosts.py for more information.
#

add_trailing_slash = Redirector(application_instance.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT)

sys.stdout.write('%s: ' % application_instance.name)
for i in range(len(hosts)):
	host, url = hosts.items()[i]
	if url is None:
		url = application_default_url
	if url != '' and url[-1] == '/':
		# No trailing slash
		url = url[:-1]
	sys.stdout.write('"%s/" on %s' % (url, host.name))
	host.attach(url, application_instance).matchingMode = Template.MODE_STARTS_WITH
	if url != '':
		host.attach(url, add_trailing_slash).matchingMode = Template.MODE_EQUALS
	if i < len(hosts) - 1:
		sys.stdout.write(', ')
print '.'

application_globals = application_instance.context.attributes

application_globals['com.threecrickets.prudence.component'] = component
cache = component.context.attributes['com.threecrickets.prudence.cache']
if cache is not None:
	application_globals['com.threecrickets.prudence.cache'] = cache

#
# Inbound root
#

router = PrudenceRouter(application_instance.context, minimum_time_between_validity_checks)
router.routingMode = Router.MODE_BEST_MATCH
application_instance.setInboundRoot(router) # Must call setInboundRoot explicitly here

#
# Add trailing slashes
#

for url in url_add_trailing_slash:
	url = fix_url(url)
	if len(url) > 0:
		if url[-1] == '/':
			url = url[:-1]
		router.attach(url, add_trailing_slash)

language_manager = executable.manager

#
# Libraries
#

libraries_document_sources = CopyOnWriteArrayList()
libraries_document_sources.add(DocumentFileSource(application_base + libraries_base_path, application_base_path + libraries_base_path, documents_default_name, 'py', minimum_time_between_validity_checks))
libraries_document_sources.add(common_libraries_document_source)

#
# Dynamic web
#

dynamic_web_document_source = DocumentFileSource(application_base + dynamic_web_base_path, application_base_path + dynamic_web_base_path, dynamic_web_default_document, 'py', minimum_time_between_validity_checks)
fragments_document_sources = CopyOnWriteArrayList()
fragments_document_sources.add(DocumentFileSource(application_base + fragments_base_path, application_base_path + fragments_base_path, dynamic_web_default_document, 'py', minimum_time_between_validity_checks))
fragments_document_sources.add(common_fragments_document_source)
cache_key_pattern_handlers = ConcurrentHashMap()
scriptlet_plugins = ConcurrentHashMap()
pass_through_documents = CopyOnWriteArraySet()
pass_through_documents.addAll(dynamic_web_pass_through)
application_globals['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = dynamic_web_document_source
application_globals['com.threecrickets.prudence.GeneratedTextResource.extraDocumentSources'] = fragments_document_sources
application_globals['com.threecrickets.prudence.GeneratedTextResource.passThroughDocuments'] = pass_through_documents
application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName'] = dynamic_web_default_document
application_globals['com.threecrickets.prudence.GeneratedTextResource.executionController'] = PhpExecutionController() # Adds PHP predefined variables
application_globals['com.threecrickets.prudence.GeneratedTextResource.clientCachingMode'] = dynamic_web_client_caching_mode
application_globals['com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers'] = cache_key_pattern_handlers
application_globals['com.threecrickets.prudence.GeneratedTextResource.scriptletPlugins'] = scriptlet_plugins

dynamic_web = Finder(application_instance.context, class_loader.loadClass('com.threecrickets.prudence.GeneratedTextResource'))
dynamic_web_base_url = fix_url(dynamic_web_base_url)
router.attachBase(fix_url(dynamic_web_base_url), dynamic_web)

if dynamic_web_defrost:
	for defrost_task in DefrostTask.forDocumentSource(dynamic_web_document_source, language_manager, 'python', True, True):
		tasks.append(defrost_task)

#
# Static web
#

static_web = Fallback(application_instance.context, minimum_time_between_validity_checks)
directory = Directory(application_instance.context, File(application_base_path + static_web_base_path).toURI().toString())
directory.listingAllowed = static_web_directory_listing_allowed
directory.negotiatingContent = True
static_web.addTarget(directory)
directory = Directory(application_instance.context, File(document.source.basePath, 'common/web/static/').toURI().toString())
directory.listingAllowed = static_web_directory_listing_allowed
directory.negotiatingContent = True
static_web.addTarget(directory)

static_web_base_url = fix_url(static_web_base_url)
if static_web_compress:
	encoder = Encoder(application_instance.context)
	encoder.next = static_web
	static_web = encoder
router.attachBase(static_web_base_url, static_web)

#
# Resources
#

resources_document_source = DocumentFileSource(application_base + resources_base_path, application_base_path + resources_base_path, documents_default_name, 'py', minimum_time_between_validity_checks)
pass_through_documents = CopyOnWriteArraySet()
pass_through_documents.addAll(resources_pass_through)
application_globals['com.threecrickets.prudence.DelegatedResource.documentSource'] = resources_document_source
application_globals['com.threecrickets.prudence.DelegatedResource.passThroughDocuments'] = pass_through_documents

resources = Finder(application_instance.context, class_loader.loadClass('com.threecrickets.prudence.DelegatedResource'))
resources_base_url = fix_url(resources_base_url)
router.attachBase(resources_base_url, resources)

if resources_defrost:
	for defrost_task in DefrostTask.forDocumentSource(resources_document_source, language_manager, 'python', False, True):
		tasks.append(defrost_task)

#
# SourceCode
#

if show_debug_on_error:
	application_globals['com.threecrickets.prudence.SourceCodeResource.documentSources'] = [dynamic_web_document_source, resources_document_source]
	source_code = Finder(application_instance.context, class_loader.loadClass('com.threecrickets.prudence.SourceCodeResource'))
	show_source_code_url = fix_url(show_source_code_url)
	router.attach(show_source_code_url, source_code).matchingMode = Template.MODE_EQUALS

#
# Preheat
#

if dynamic_web_preheat:
	for preheat_task in PreheatTask.forDocumentSource(dynamic_web_document_source, application_internal_name, application_instance, application_logger_name):
		tasks.append(preheat_task)

for preheat_resource in preheat_resources:
	tasks.append(PreheatTask(application_internal_name, preheat_resource, application_instance, application_logger_name))
