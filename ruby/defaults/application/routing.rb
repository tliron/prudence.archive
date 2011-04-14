#
# Prudence Application Routing
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

import java.lang.ClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import org.restlet.routing.Router
import org.restlet.routing.Redirector
import org.restlet.routing.Template
import org.restlet.resource.Finder
import org.restlet.resource.Directory
import org.restlet.engine.application.Encoder
import com.threecrickets.scripturian.util.DefrostTask
import com.threecrickets.scripturian.document.DocumentFileSource
import com.threecrickets.prudence.PrudenceRouter
import com.threecrickets.prudence.util.Fallback
import com.threecrickets.prudence.util.PreheatTask
import com.threecrickets.prudence.util.PhpExecutionController

$class_loader = ClassLoader::system_class_loader

#
# Utilities
#

# Makes sure we have slashes where we expect them
def fix_url url
	url = url.gsub /\/\//, '/' # no doubles
	if url == '' || url[0] != ?/ # always at the beginning
		url = '/' + url
	end
	if url != '/' && url[-1] != ?/ # always at the end
		url += '/'
	end
	return url
end

#
# Internal router
#

$component.internal_router.attach('/' + $application_internal_name, $application_instance).matching_mode = Template::MODE_STARTS_WITH

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See defaults/instance/hosts.rb for more information.
#

$add_trailing_slash = Redirector.new($application_instance.context, '{ri}/', Redirector::MODE_CLIENT_PERMANENT)

print "#{$application_instance.name}: "
$hosts.each_with_index do |entry, i|
	host = entry[0]
	url = entry[1]
	if url.nil?
		url = $application_default_url
	end
	if url != '' && url[-1] == ?/
		# No trailing slash
		url = url[0..-2]
	end
	print "\"#{url}/\" on #{host.name}"
	host.attach(url, $application_instance).matching_mode = Template::MODE_STARTS_WITH
	if url != ''
		host.attach(url, $add_trailing_slash).matching_mode = Template::MODE_EQUALS
	end
	if i < $hosts.length - 1
		print ', '
	end
end
puts '.'

$application_globals = $application_instance.context.attributes

$application_globals['com.threecrickets.prudence.component'] = $component
$cache = $component.context.attributes['com.threecrickets.prudence.cache']
if !$cache.nil?
	$application_globals['com.threecrickets.prudence.cache'] = $cache
end

#
# Inbound root
#

$router = PrudenceRouter.new($application_instance.context, $minimum_time_between_validity_checks)
$router.routing_mode = Router::MODE_BEST_MATCH
$application_instance.inbound_root = $router

#
# Add trailing slashes
#

for url in $url_add_trailing_slash
	url = fix_url url
	if url.length > 0
		if url[-1] == ?/
			# Remove trailing slash for pattern
			url = url[0..-2]
		end
		$router.attach url, $add_trailing_slash
	end
end

$language_manager = $executable.manager

#
# Libraries
#

$libraries_document_sources = CopyOnWriteArrayList.new
$libraries_document_sources << DocumentFileSource.new($application_base + $libraries_base_path, $application_base_path + $libraries_base_path, $documents_default_name, 'rb', $minimum_time_between_validity_checks)
$libraries_document_sources << $common_libraries_document_source

#
# Dynamic web
#

$dynamic_web_document_source = DocumentFileSource.new($application_base + $dynamic_web_base_path, $application_base_path + $dynamic_web_base_path, $dynamic_web_default_document, 'rb', $minimum_time_between_validity_checks)
$fragments_document_sources = CopyOnWriteArrayList.new
$fragments_document_sources << DocumentFileSource.new($application_base + $fragments_base_path, $application_base_path + $fragments_base_path, $dynamic_web_default_document, 'rb', $minimum_time_between_validity_checks)
$fragments_document_sources << $common_fragments_document_source
$cache_key_pattern_handlers = ConcurrentHashMap.new
$scriptlet_plugins = ConcurrentHashMap.new
$application_globals['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = $dynamic_web_document_source
$application_globals['com.threecrickets.prudence.GeneratedTextResource.extraDocumentSources'] = $fragments_document_sources
$application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName'] = $dynamic_web_default_document
$application_globals['com.threecrickets.prudence.GeneratedTextResource.executionController'] = PhpExecutionController.new # Adds PHP predefined variables
$application_globals['com.threecrickets.prudence.GeneratedTextResource.clientCachingMode'] = $dynamic_web_client_caching_mode
$application_globals['com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers'] = $cache_key_pattern_handlers
$application_globals['com.threecrickets.prudence.GeneratedTextResource.scriptletPlugins'] = $scriptlet_plugins

$dynamic_web = Finder.new($application_instance.context, $class_loader.load_class('com.threecrickets.prudence.GeneratedTextResource'))
$dynamic_web_base_url = fix_url $dynamic_web_base_url
$router.attach_base $dynamic_web_base_url, $dynamic_web

if $dynamic_web_defrost
	defrost_tasks = DefrostTask::for_document_source $dynamic_web_document_source, $language_manager, 'ruby', true, true
	for defrost_task in defrost_tasks
		$tasks << defrost_task
	end
end

#
# Static web
#

$static_web = Fallback.new($application_instance.context, $minimum_time_between_validity_checks)
$directory = Directory.new($application_instance.context, java.io.File.new($application_base_path + $static_web_base_path).to_uri.to_string)
$directory.listing_allowed = $static_web_directory_listing_allowed
$directory.negotiating_content = true
$static_web.add_target $directory
$directory = Directory.new($application_instance.context, java.io.File.new($document.source.base_path + 'common/web/static/').to_uri.to_string)
$directory.listing_allowed = $static_web_directory_listing_allowed
$directory.negotiating_content = true
$static_web.add_target $directory

$static_web_base_url = fix_url $static_web_base_url
if $static_web_compress
	encoder = Encoder.new($application_instance.context)
	encoder.next = $static_web
	$static_web = encoder
end
$router.attach_base $static_web_base_url, $static_web

#
# Resources
#

$resources_document_source = DocumentFileSource.new($application_base + $resources_base_path, $application_base_path + $resources_base_path, $documents_default_name, 'rb', $minimum_time_between_validity_checks)
$application_globals['com.threecrickets.prudence.DelegatedResource.documentSource'] = $resources_document_source

$resources = Finder.new($application_instance.context, $class_loader.load_class('com.threecrickets.prudence.DelegatedResource'))
$resources_base_url = fix_url $resources_base_url
$router.attach_base $resources_base_url, $resources

if $resources_defrost
	defrost_tasks = DefrostTask::for_document_source $resources_document_source, $language_manager, 'ruby', false, true
	for defrost_task in defrost_tasks
		$tasks << defrost_task
	end
end

#
# SourceCode
#

if $show_debug_on_error
	$application_globals['com.threecrickets.prudence.SourceCodeResource.documentSources'] = [$dynamic_web_document_source, $resources_document_source]
	$source_code = Finder.new($application_instance.context, $class_loader.load_class('com.threecrickets.prudence.SourceCodeResource'))
	$show_source_code_url = fix_url $show_source_code_url
	$router.attach($show_source_code_url, $source_code).matching_mode = Template::MODE_EQUALS
end

#
# Preheat
#

if $dynamic_web_preheat
	preheat_tasks = PreheatTask::for_document_source $dynamic_web_document_source, $application_internal_name, $application_instance, $application_logger_name
	for preheat_task in preheat_tasks
		$tasks << preheat_task
	end
end

for preheat_resource in $preheat_resources
	$tasks << PreheatTask.new($application_internal_name, preheat_resource, $application_instance, $application_logger_name)
end
