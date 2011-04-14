<?php
//
// Prudence Application Routing
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

import java.lang.ClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.restlet.routing.Router;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;
import org.restlet.resource.Finder;
import org.restlet.resource.Directory;
import org.restlet.engine.application.Encoder;
import com.threecrickets.scripturian.util.DefrostTask;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.prudence.PrudenceRouter;
import com.threecrickets.prudence.util.Fallback;
import com.threecrickets.prudence.util.PreheatTask;
import com.threecrickets.prudence.util.PhpExecutionController;

global $executable, $component, $tasks, $application_instance, $application_globals, $language_manager;
global $application_internal_name, $application_logger_name, $application_base, $application_base_path, $application_default_url;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $show_debug_on_error, $show_source_code_url;
global $hosts, $documents_default_name, $minimum_time_between_validity_checks;
global $libraries_document_sources, $libraries_base_path;
global $resources_base_url, $resources_base_path, $resources_defrost;
global $dynamic_web_base_url, $dynamic_web_base_path, $fragments_base_path, $dynamic_web_default_document, $dynamic_web_defrost, $dynamic_web_preheat, $dynamic_web_client_caching_mode;
global $cache_key_pattern_handlers, $scriptlet_plugins;
global $static_web_base_url, $static_web_base_path, $static_web_compress, $static_web_directory_listing_allowed;
global $preheat_resources;
global $url_add_trailing_slash;
global $predefined_globals;
global $common_libraries_document_source, $common_fragments_document_source, $common_tasks_document_sources, $common_handlers_document_sources;

$class_loader = ClassLoader::getSystemClassLoader();

//
// Utilities
//

// Makes sure we have slashes where we expect them
if(!function_exists('fix_url')) {
	function fix_url($url) {
		$url = str_replace('//', '/', $url); // no doubles
		if($url == '' || $url[0] != '/') { // always at the beginning
			$url = '/' . $url;
		}
		if(($url != '/') && ($url[strlen($url) - 1] != '/')) { // always at the end
			$url = $url . '/';
		}
		return $url;
	}
}

//
// Internal router
//

$component->internalRouter->attach('/' . $application_internal_name, $application_instance)->matchingMode = Template::MODE_STARTS_WITH;

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
	if(($url != '') && ($url[strlen($url) - 1] == '/')) {
		// No trailing slash
		$url = substr($url, 0, -1);
	}
	print '"' . $url . '/" on ' . $host->name;
	$host->attach($url, $application_instance)->matchingMode = Template::MODE_STARTS_WITH;
	if($url != '') {
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

$router = new PrudenceRouter($application_instance->context, $minimum_time_between_validity_checks);
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
		$router->attach($url, $add_trailing_slash);
	}
}

$language_manager = $executable->manager;

//
// Libraries
//

$libraries_document_sources = new CopyOnWriteArrayList();
$libraries_document_sources->add(new DocumentFileSource($application_base . $libraries_base_path, $application_base_path . $libraries_base_path, $documents_default_name, 'php', $minimum_time_between_validity_checks));
$libraries_document_sources->add($common_libraries_document_source);

//
// Dynamic web
//

$dynamic_web_document_source = new DocumentFileSource($application_base . $dynamic_web_base_path, $application_base_path . $dynamic_web_base_path, $dynamic_web_default_document, 'php', $minimum_time_between_validity_checks);
$fragments_document_sources = new CopyOnWriteArrayList();
$fragments_document_sources->add(new DocumentFileSource($application_base . $fragments_base_path, $application_base_path . $fragments_base_path, $dynamic_web_default_document, 'php', $minimum_time_between_validity_checks));
$fragments_document_sources->add($common_fragments_document_source);
$cache_key_pattern_handlers = new ConcurrentHashMap();
$scriptlet_plugins = new ConcurrentHashMap();
$application_globals['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = $dynamic_web_document_source;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.extraDocumentSources'] = $fragments_document_sources;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName'] = $dynamic_web_default_document;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.executionController'] = new PhpExecutionController(); // Adds PHP predefined variables
$application_globals['com.threecrickets.prudence.GeneratedTextResource.clientCachingMode'] = $dynamic_web_client_caching_mode;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers'] = $cache_key_pattern_handlers;
$application_globals['com.threecrickets.prudence.GeneratedTextResource.scriptletPlugins'] = $scriptlet_plugins;

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

$static_web = new Fallback($application_instance->context, $minimum_time_between_validity_checks);
$directory = new Directory($application_instance->context, new File($application_base_path . $static_web_base_path)->toURI()->toString());
$directory->listingAllowed = $static_web_directory_listing_allowed;
$directory->negotiatingContent = TRUE;
$static_web->addTarget($directory);
$directory = new Directory($application_instance->context, new File($document.source.basePath, 'common/web/static/')->toURI()->toString());
$directory->listingAllowed = $static_web_directory_listing_allowed;
$directory->negotiatingContent = TRUE;
$static_web->addTarget($directory);

$static_web_base_url = fix_url($static_web_base_url);
if ($static_web_compress) {
	$encoder = new Encoder($application_instance->context);
	$encoder->next = $static_web;
	$static_web = $encoder;
}
$router->attachBase($static_web_base_url, $static_web);

//
// Resources
//

$resources_document_source = new DocumentFileSource($application_base . $resources_base_path, $application_base_path . $resources_base_path, $documents_default_name, 'php', $minimum_time_between_validity_checks);
$application_globals['com.threecrickets.prudence.DelegatedResource.documentSource'] = $resources_document_source;

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
	$document_sources = new ArrayList(2);
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
	$preheat_tasks = PreheatTask::forDocumentSource($dynamic_web_document_source, $application_internal_name, $application_instance, $application_logger_name);
	foreach($preheat_tasks as $preheat_task) {
		$tasks[] = $preheat_task;
	}
}

foreach($preheat_resources as $preheat_resource) {
	$tasks[] = new PreheatTask($application_internal_name, $preheat_resource, $application_instance, $application_logger_name);
}
?>