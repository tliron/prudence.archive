#
# Prudence Application Routing
#

import java.lang.ClassLoader
import java.util.concurrent.ConcurrentHashMap
import org.restlet.routing.Router
import org.restlet.routing.Redirector
import org.restlet.routing.Template
import org.restlet.resource.Finder
import org.restlet.resource.Directory
import com.threecrickets.scripturian.util.DefrostTask
import com.threecrickets.scripturian.document.DocumentFileSource
import com.threecrickets.prudence.PrudenceRouter
import com.threecrickets.prudence.util.PreheatTask
import com.threecrickets.prudence.util.PhpExecutionController

$class_loader = ClassLoader::system_class_loader

#
# Utilities
#

# Makes sure we have slashes where we expect them
def fix_url url
	url = url.gsub /\/\//, '/' # no doubles
	if url.length > 0 && url[0] == ?/ # never at the beginning
		url = url[1..-1]
	end
	if url.length > 0 && url[-1] != ?/ # always at the end
		url += '/'
	end
	return url
end

#
# Internal router
#

$component.internal_router.attach('/' + $application_internal_name + '/', $application_instance).matching_mode = Template::MODE_STARTS_WITH

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See defaults/instance/hosts.rb for more information.
#

$add_trailing_slash = Redirector.new($application_instance.context, '{ri}/', Redirector::MODE_CLIENT_PERMANENT)

print $application_instance.name + ': '
i = 0
for entry in $hosts
	host = entry[0]
	url = entry[1]
	if url.nil?
		url = $application_default_url
	end
	print '"' + url + '" on ' + host.name
	host.attach(url, $application_instance).matching_mode = Template::MODE_STARTS_WITH
	if url != '/'
		if url[-1] == ?/
			url = url[0..-2]
		end
		host.attach(url, $add_trailing_slash).matching_mode = Template::MODE_EQUALS
	end
	if i < $hosts.length - 1
		print ', '
	end
	i += 1
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

$router = PrudenceRouter.new $application_instance.context
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

//
// Handlers
//

$handlers_document_source = DocumentFileSource.new($application_base_path + $handlers_base_path, $handlers_default_name, 'rb', $handlers_minimum_time_between_validity_checks)
$router.filter_document_source = $handlers_document_source
$router.filter_language_manager = $language_manager

#
# Dynamic web
#

$dynamic_web_document_source = DocumentFileSource.new($application_base_path + $dynamic_web_base_path, $dynamic_web_default_document, 'rb', $dynamic_web_minimum_time_between_validity_checks)
$cache_key_pattern_handlers = ConcurrentHashMap.new
$application_globals['com.threecrickets.prudence.GeneratedTextResource.languageManager'] = $language_manager
$application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag'] = 'ruby'
$application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = $dynamic_web_default_document
$application_globals['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = $dynamic_web_document_source
$application_globals['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = $dynamic_web_source_viewable
$application_globals['com.threecrickets.prudence.GeneratedTextResource.executionController'] = PhpExecutionController.new # Adds PHP predefined variables
$application_globals['com.threecrickets.prudence.GeneratedTextResource.clientCachingMode'] = $dynamic_web_client_caching_mode
$application_globals['com.threecrickets.prudence.GeneratedTextResource.fileUploadSizeThreshold'] = $file_upload_size_threshold
$application_globals['com.threecrickets.prudence.GeneratedTextResource.handlersDocumentSource'] = $handlers_document_source
$application_globals['com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers'] = $cache_key_pattern_handlers

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

$static_web = Directory.new($application_instance.context, java.io.File.new($application_base_path + $static_web_base_path).to_uri.to_string)
$static_web.listing_allowed = $static_web_directory_listing_allowed
$static_web.negotiating_content = true
$static_web_base_url = fix_url $static_web_base_url
$router.attach_base $static_web_base_url, $static_web

#
# Resources
#

$resources_document_source = DocumentFileSource.new($application_base_path + $resources_base_path, $resources_default_name, 'rb', $resources_minimum_time_between_validity_checks)
$application_globals['com.threecrickets.prudence.DelegatedResource.languageManager'] = $language_manager
$application_globals['com.threecrickets.prudence.DelegatedResource.defaultLanguageTag'] = 'ruby'
$application_globals['com.threecrickets.prudence.DelegatedResource.defaultName'] = $resources_default_name
$application_globals['com.threecrickets.prudence.DelegatedResource.documentSource'] = $resources_document_source
$application_globals['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = $resources_source_viewable
$application_globals['com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold'] = $file_upload_size_threshold

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
	preheat_tasks = PreheatTask::for_document_source $dynamic_web_document_source, $component.context, $application_internal_name
	for preheat_task in preheat_tasks
		$tasks << preheat_task
	end
end

for preheat_resource in $preheat_resources
	$tasks << PreheatTask.new($component.context, $application_internal_name, preheat_resource)
end
