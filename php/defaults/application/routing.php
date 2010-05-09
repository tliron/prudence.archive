<?php
//
// Prudence Application Routing
//

import java.lang.ClassLoader;
import java.io.File;
import java.util.ArrayList;
import org.restlet.routing.Router;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;
import org.restlet.resource.Finder;
import org.restlet.resource.Directory;
import com.threecrickets.scripturian.util.DefrostTask;
import com.threecrickets.scripturian.file.DocumentFileSource;
import com.threecrickets.prudence.util.PrudenceRouter;
import com.threecrickets.prudence.util.PreheatTask;
import com.threecrickets.prudence.util.PhpExecutionController;

global $executable, $component, $tasks, $application_instance, $attributes;
global $application_internal_name, $application_logger_name, $application_base_path, $application_default_url;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $show_debug_on_error, $show_source_code_url;
global $hosts;
global $resources_base_url, $resources_base_path, $resources_default_name, $resources_defrost, $resources_source_viewable, $resources_minimum_time_between_validity_checks;
global $dynamic_web_base_url, $dynamic_web_base_path, $dynamic_web_default_document, $dynamic_web_defrost, $dynamic_web_preheat, $dynamic_web_source_viewable, $dynamic_web_minimum_time_between_validity_checks;
global $static_web_base_url, $static_web_base_path, $static_web_directory_listing_allowed;
global $preheat_resources;
global $url_add_trailing_slash;
global $runtime_attributes;

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

$attributes = $application_instance->context->attributes;

$attributes['component'] = $component;
$attributes['com.threecrickets.prudence.cache'] = $component->context->attributes['com.threecrickets.prudence.cache'];

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

//
// Dynamic web
//

$language_manager = $executable->manager;
$dynamic_web_document_source = new DocumentFileSource($application_base_path . $dynamic_web_base_path, $dynamic_web_default_document, $dynamic_web_minimum_time_between_validity_checks);
$attributes['com.threecrickets.prudence.GeneratedTextResource.languageManager'] = $language_manager;
$attributes['com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag'] = 'php';
$attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = $dynamic_web_default_document;
$attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = $dynamic_web_document_source;
$attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = $dynamic_web_source_viewable;
$attributes['com.threecrickets.prudence.GeneratedTextResource.executionController'] = new PhpExecutionController(); // Adds PHP predefined variables

$dynamic_web = new Finder($application_instance->context, $class_loader->loadClass('com.threecrickets.prudence.GeneratedTextResource'));
$router->attachBase(fix_url($dynamic_web_base_url), $dynamic_web);

if($dynamic_web_defrost) {
	$defrost_tasks = DefrostTask::forDocumentSource($dynamic_web_document_source, $language_manager, 'php', true, true);
	foreach($defrost_tasks as $defrost_task) {
		$tasks[] = $defrost_task;
	}
}

//
// Static web
//

$static_web = new Directory($router->context, new File($application_base_path . $static_web_base_path)->toURI()->toString());
$static_web->listingAllowed = $static_web_directory_listing_allowed;
$static_web->negotiatingContent = true;
$router->attachBase(fix_url($static_web_base_url), $static_web);

//
// Resources
//

$resources_document_source = new DocumentFileSource($application_base_path . $resources_base_path, $resources_default_name, $resources_minimum_time_between_validity_checks);
$attributes['com.threecrickets.prudence.DelegatedResource.languageManager'] = $language_manager;
$attributes['com.threecrickets.prudence.DelegatedResource.defaultLanguageTag'] = 'php';
$attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = $resources_default_name;
$attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = $resources_document_source;
$attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = $resources_source_viewable;

$resources = new Finder($application_instance->context, $class_loader->loadClass('com.threecrickets.prudence.DelegatedResource'));
$router->attachBase(fix_url($resources_base_url), $resources);

if($resources_defrost) {
	$defrost_tasks = DefrostTask::forDocumentSource($resources_document_source, $language_manager, 'php', false, true);
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
	$attributes['com.threecrickets.prudence.SourceCodeResource.documentSources'] = $document_sources;
	$source_code = new Finder($application_instance->context, $class_loader->loadClass('com.threecrickets.prudence.SourceCodeResource'));
	$router->attach(fix_url($show_source_code_url), $source_code)->matchingMode = Template::MODE_EQUALS;
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