#
# Prudence Application Routing
#

import java.lang.ClassLoader
import org.restlet.routing.Router
import org.restlet.routing.Redirector
import org.restlet.routing.Template
import org.restlet.resource.Finder
import org.restlet.resource.Directory
import com.threecrickets.scripturian.util.DefrostTask
import com.threecrickets.scripturian.file.DocumentFileSource
import com.threecrickets.prudence.util.PrudenceRouter
import com.threecrickets.prudence.util.PreheatTask

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

$component.internal_router.attach('/' + $application_internal_name + '/', $application).matching_mode = Template::MODE_STARTS_WITH

#
# Hosts
#
# Note that the application's context will not be created until we attach the application to at least one
# virtual host. See defaults/instance/hosts.rb for more information.
#

$add_trailing_slash = Redirector.new($application.context, '{ri}/', Redirector::MODE_CLIENT_PERMANENT)

print $application.name + ': '
i = 0
for entry in $hosts
	host = entry[0]
	url = entry[1]
	if url.nil?
		url = $application_default_url
	end
	print '"' + url + '" on ' + host.name
	host.attach(url, $application).matching_mode = Template::MODE_STARTS_WITH
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

$attributes = $application.context.attributes

$attributes['component'] = $component

#
# Inbound root
#

$router = PrudenceRouter.new $application.context
$router.routing_mode = Router::MODE_BEST_MATCH
$application.inbound_root = $router

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

#
# Dynamic web
#

$language_manager = $executable.context.manager
$dynamic_web_document_source = DocumentFileSource.new($application_base_path + $dynamic_web_base_path, $dynamic_web_default_document, $dynamic_web_minimum_time_between_validity_checks)
$attributes['com.threecrickets.prudence.GeneratedTextResource.languageManager'] = $language_manager
$attributes['com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag'] = 'ruby'
$attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = $dynamic_web_default_document
$attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = $dynamic_web_document_source
$attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = $dynamic_web_source_viewable

$dynamic_web = Finder.new($application.context, $class_loader.load_class('com.threecrickets.prudence.GeneratedTextResource'))
$router.attach_base fix_url($dynamic_web_base_url), $dynamic_web

if $dynamic_web_defrost
	defrost_tasks = DefrostTask::for_document_source $dynamic_web_document_source, $language_manager, true, true
	for defrost_task in defrost_tasks
		$tasks << defrost_task
	end
end

#
# Static web
#

$static_web = Directory.new($router.context, java.io.File.new($application_base_path + $static_web_base_path).to_uri.to_string)
$static_web.listing_allowed = $static_web_directory_listing_allowed
$static_web.negotiating_content = true
$router.attach_base fix_url($static_web_base_url), $static_web

#
# Resources
#

$resources_document_source = DocumentFileSource.new($application_base_path + $resources_base_path, $resources_default_name, $resources_minimum_time_between_validity_checks)
$attributes['com.threecrickets.prudence.DelegatedResource.languageManager'] = $language_manager
$attributes['com.threecrickets.prudence.DelegatedResource.defaultLanguageTag'] = 'ruby'
$attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = $resources_default_name
$attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = $resources_document_source
$attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = $resources_source_viewable

$resources = Finder.new($application.context, $class_loader.load_class('com.threecrickets.prudence.DelegatedResource'))
$router.attach_base fix_url($resources_base_url), $resources

if $resources_defrost
	defrost_tasks = DefrostTask::for_document_source $resources_document_source, $language_manager, false, true
	for defrost_task in defrost_tasks
		$tasks << defrost_task
	end
end

#
# SourceCode
#

if $show_debug_on_error
	$attributes['com.threecrickets.prudence.SourceCodeResource.documentSources'] = [$dynamic_web_document_source, $resources_document_source]
	$source_code = Finder.new($application.context, $class_loader.load_class('com.threecrickets.prudence.SourceCodeResource'))
	$router.attach(fix_url($show_source_code_url), $source_code).matching_mode = Template::MODE_EQUALS
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
