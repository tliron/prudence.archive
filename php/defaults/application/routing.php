<?php
//
// Prudence Application Routing
//

import java.lang.ClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.restlet.routing.Router;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;
import org.restlet.resource.Finder;
import org.restlet.resource.Directory;
import org.restlet.engine.application.Encoder;
import com.threecrickets.scripturian.util.DefrostTask;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.prudence.PrudenceRouter;
import com.threecrickets.prudence.util.PreheatTask;
import com.threecrickets.prudence.util.PhpExecutionController;

global $executable, $component, $tasks, $application_instance, $application_globals, $language_manager;
global $application_internal_name, $application_logger_name, $application_base, $application_base_path, $application_default_url;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $show_debug_on_error, $show_source_code_url;
global $hosts;
global $resources_base_url, $resources_base_path, $resources_default_name, $resources_defrost, $resources_source_viewable, $resources_minimum_time_between_validity_checks;
global $dynamic_web_base_url, $dynamic_web_base_path, $dynamic_web_default_document, $dynamic_web_defrost, $dynamic_web_preheat, $dynamic_web_source_viewable, $dynamic_web_minimum_time_between_validity_checks, $dynamic_web_client_caching_mode;
global $cache_key_pattern_handlers;
global $static_web_base_url, $static_web_base_path, $static_web_compress, $static_web_directory_listing_allowed;
global $file_upload_size_threshold;
global $handlers_base_path, $handlers_default_name, $handlers_minimum_time_between_validity_checks;
global $preheat_resources;
global $url_add_trailing_slash;
global $predefined_globals;

$class_loader = ClassLoader::getSystemClassLoader();

//
// Utilities
//

// Makes sure we have slashes where we expect them
if(!function_exists('fix_url')) {
	function fix_url($url) {
		$url = str_replace('//', '/', $url); // no doubles
		if(strlen($url) > 0 && $url[0] == '/') { // never at the beginning
			$url = substr($url, 1);
		}
		if(strlen($url) > 0 && $url[strlen($url) - 1] != '/') { // always at the end
			$url = $url . '/';
		}
		return $url;
	}
}

//
// Internal router
//

$component->internalRouter->attach('/' . $application_internal_name . '/', $application_instance)->matchingMode = Template::MODE_STARTS_WITH;

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See defaults/instance/hosts.php for more information.
//

$add_trailing_slash = new Redirector($application_instance->context, '{ri}/', Redirector::MODE_CLIENT_PERMANENT);

print $application_instance->name . ': '
$i = 0;
foreach($hosts as $entry) {
	$host = $entry[0];
	$url = $entry[1];
	if(is_null($url)) {
		$url = $application_default_url;
	}
	print '"' . $url . '" on ' . $host->name;
	$host->attach($url, $application_instance)->matchingMode = Template::MODE_STARTS_WITH;
	if($url != '/') {
		if($url[strlen($url) - 1] == '/') {
			$url = substr($url, 0, -1);
		}
		$host->attach($url, $add_trailing_slash)->matchingMode = Template::MODE_EQUALS;
	}
	if($i < count($hosts) - 1) {
		print ', ';
	}
	$i++;
}
print ".\n";

$application_globals = $application_instance->context->attributes;

$application_globals['com.threecrickets.prudence.component'] = $component;
$cache = $component->context->attributes['com.threecrickets.prudence.cache'];
if($cache) {
	$application_globals['com.threecrickets.prudence.cache'] = $cache;
}

//
// Inbound root
//

$router = new PrudenceRouter($application_instance->context);
$router->routingMode = Router::MODE_BEST_MATCH;
$application_instance->inboundRoot = $router;

//
// Add trailing slashes
//

foreach($url_add_trailing_slash as $url) {
	$url = fix_url($url);
	if(strlen($url) > 0) {
		if($url[strlen($url) - 1] == '/') {
			// Remove trailing slash for pattern
			$url = substr($url, 0, -1);
		}
		$router->attach($url, $addTrailingSlash);
	}
}

$language_manager = $executable->manager;

//
// Handlers
//

$handlers_document_source = new DocumentFileSource($application_base . $handlers_base_path, $application_base_path . $handlers_base_path, $handlers_default_name, 'php', $handlers_minimum_time_between_validity_checks);
$router->filterDocumentSource = $handlers_document_source;
$router->filterLanguageManager = $language_manager;

//
// Dynamic web
//

$dynamic_web_document_source = new DocumentFileSource($application_base . $dynamic_web_base_path, $application_base_path . $dynamic_web_base_path, $dynamic_web_default_document, 'php', $dynamic_web_minimum_time_between_validity_checks);
$cache_key_pattern_handlers = new ConcurrentHashMap();
$application_globals['com.threecrickets.prudence.GeneratedTextResource.languageManager'] = $language_manager;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag'] = 'php';
$application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = $dynamic_web_default_document;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = $dynamic_web_document_source;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = $dynamic_web_source_viewable;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.executionController'] = new PhpExecutionController(); // Adds PHP predefined variables
$application_globals['com.threecrickets.prudence.GeneratedTextResource.clientCachingMode'] = $dynamic_web_client_caching_mode;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.fileUploadSizeThreshold'] = $file_upload_size_threshold;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.handlersDocumentSource'] = $handlers_document_source;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers'] = $cache_key_pattern_handlers;

$dynamic_web = new Finder($application_instance->context, $class_loader->loadClass('com.threecrickets.prudence.GeneratedTextResource'));
$dynamic_web_base_url = fix_url($dynamic_web_base_url);
$router->attachBase($dynamic_web_base_url, $dynamic_web);

if($dynamic_web_defrost) {
	$defrost_tasks = DefrostTask::forDocumentSource($dynamic_web_document_source, $language_manager, 'php', TRUE, TRUE);
	foreach($defrost_tasks as $defrost_task) {
		$tasks[] = $defrost_task;
	}
}

//
// Static web
//

$static_web = new Directory($application_instance->context, new File($application_base_path . $static_web_base_path)->toURI()->toString());
$static_web->listingAllowed = $static_web_directory_listing_allowed;
$static_web->negotiatingContent = TRUE;
$static_web_base_url = fix_url($static_web_base_url);
if ($static_web_compress) {
	$router->filterBase($static_web_base_url, new Encoder(NULL), $static_web)
}
else {
	$router->attachBase($static_web_base_url, $static_web);
} 

//
// Resources
//

$resources_document_source = new DocumentFileSource($application_base . $resources_base_path, $application_base_path . $resources_base_path, $resources_default_name, 'php', $resources_minimum_time_between_validity_checks);
$application_globals['com.threecrickets.prudence.DelegatedResource.languageManager'] = $language_manager;
$application_globals['com.threecrickets.prudence.DelegatedResource.defaultLanguageTag'] = 'php';
$application_globals['com.threecrickets.prudence.DelegatedResource.defaultName'] = $resources_default_name;
$application_globals['com.threecrickets.prudence.DelegatedResource.documentSource'] = $resources_document_source;
$application_globals['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = $resources_source_viewable;
$application_globals['com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold'] = $file_upload_size_threshold;

$resources = new Finder($application_instance->context, $class_loader->loadClass('com.threecrickets.prudence.DelegatedResource'));
$resources_base_url = fix_url($resources_base_url);
$router->attachBase($resources_base_url, $resources);

if($resources_defrost) {
	$defrost_tasks = DefrostTask::forDocumentSource($resources_document_source, $language_manager, 'php', FALSE, TRUE);
	foreach($defrost_tasks as $defrost_task) {
		$tasks[] = $defrost_task;
	}
}

//
// SourceCode
//

if($show_debug_on_error) {
	$document_sources = new ArrayList();
	$document_sources->add($dynamic_web_document_source);
	$document_sources->add($resources_document_source);
	$application_globals['com.threecrickets.prudence.SourceCodeResource.documentSources'] = $document_sources;
	$source_code = new Finder($application_instance->context, $class_loader->loadClass('com.threecrickets.prudence.SourceCodeResource'));
	$show_source_code_url = fix_url($show_source_code_url);
	$router->attach($show_source_code_url, $source_code)->matchingMode = Template::MODE_EQUALS;
}

//
// Preheat
//

if($dynamic_web_preheat) {
	$preheat_tasks = PreheatTask::forDocumentSource($dynamic_web_document_source, $component->context, $application_internal_name);
	foreach($preheat_tasks as $preheat_task) {
		$tasks[] = $preheat_task;
	}
}

foreach($preheat_resources as $preheat_resource) {
	$tasks[] = new PreheatTask($component->context, $application_internal_name, $preheat_resource);
}
?>