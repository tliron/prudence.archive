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

global $component, $tasks, $application, $attributes;
global $applicationInternalName, $applicationLoggerName, $applicationBasePath, $applicationDefaultURL;
global $applicationName, $applicationDescription, $applicationAuthor, $applicationHomeURL, $applicationContactEmail;
global $showDebugOnError, $showSourceCodeURL;
global $applicationLoggerName;
global $hosts;
global $resourcesBaseURL, $resourcesBasePath, $resourcesDefaultName, $resourcesDefrost, $resourcesSourceViewable, $resourcesMinimumTimeBetweenValidityChecks;
global $dynamicWebBaseURL, $dynamicWebBasePath, $dynamicWebDefaultDocument, $dynamicWebDefrost, $dynamicWebPreheat, $dynamicWebSourceViewable, $dynamicWebMinimumTimeBetweenValidityChecks;
global $staticWebBaseURL, $staticWebBasePath, $staticWebDirectoryListingAllowed;
global $preheatResources;
global $urlAddTrailingSlash;
global $runtimeAttributes;

$classLoader = ClassLoader::getSystemClassLoader();

//
// Utilities
//

// Makes sure we have slashes where we expect them
if(!function_exists('fixURL')) {
	function fixURL($url) {
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

$component->internalRouter->attach('/' . $applicationInternalName . '/', $application)->matchingMode = Template::MODE_STARTS_WITH;

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See defaults/instance/hosts.php for more information.
//

$addTrailingSlash = new Redirector($application->context, '{ri}/', Redirector::MODE_CLIENT_PERMANENT);

print $application->name . ': '
$i = 0;
foreach($hosts as $entry) {
	$host = $entry[0];
	$url = $entry[1];
	if(is_null($url)) {
		$url = $applicationDefaultURL;
	}
	print '"' . $url . '" on ' . $host->name;
	$host->attach($url, $application)->matchingMode = Template::MODE_STARTS_WITH;
	if($url != '/') {
		if($url[strlen($url) - 1] == '/') {
			$url = substr($url, 0, -1);
		}
		$host->attach($url, $addTrailingSlash)->matchingMode = Template::MODE_EQUALS;
	}
	if($i < count($hosts) - 1) {
		print ', ';
	}
	$i++;
}
print ".\n";

$attributes = $application->context->attributes;

$attributes->put('component', $component);

//
// Inbound root
//

$router = new PrudenceRouter($application->context);
$router->routingMode = Router::MODE_BEST_MATCH;
$application->inboundRoot = $router;

//
// Add trailing slashes
//

foreach($urlAddTrailingSlash as $url) {
	$url = fixURL($url);
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

$languageManager = $executable->context->manager;
$dynamicWebDocumentSource = new DocumentFileSource($applicationBasePath . $dynamicWebBasePath, $dynamicWebDefaultDocument, $dynamicWebMinimumTimeBetweenValidityChecks);
$attributes->put('com.threecrickets.prudence.GeneratedTextResource.languageManager', $languageManager);
$attributes->put('com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag', 'php');
$attributes->put('com.threecrickets.prudence.GeneratedTextResource.defaultName', $dynamicWebDefaultDocument);
$attributes->put('com.threecrickets.prudence.GeneratedTextResource.documentSource',$dynamicWebDocumentSource);
$attributes->put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', $dynamicWebSourceViewable);

$dynamicWeb = new Finder($application->context, $classLoader->loadClass('com.threecrickets.prudence.GeneratedTextResource'));
$router->attachBase(fixURL($dynamicWebBaseURL), $dynamicWeb);

if($dynamicWebDefrost) {
	$defrostTasks = DefrostTask::forDocumentSource($dynamicWebDocumentSource, $languageManager, true, true);
	foreach($defrostTasks as $defrostTask) {
		array_push($tasks, $defrostTask);
	}
}

//
// Static web
//

$staticWeb = new Directory($router->context, new File($applicationBasePath . $staticWebBasePath)->toURI()->toString());
$staticWeb->listingAllowed = $staticWebDirectoryListingAllowed;
$staticWeb->negotiatingContent = true;
$router->attachBase(fixURL($staticWebBaseURL), $staticWeb);

//
// Resources
//

$resourcesDocumentSource = new DocumentFileSource($applicationBasePath . $resourcesBasePath, $resourcesDefaultName, $resourcesMinimumTimeBetweenValidityChecks);
$attributes->put('com.threecrickets.prudence.DelegatedResource.languageManager', $languageManager);
$attributes->put('com.threecrickets.prudence.DelegatedResource.defaultLanguageTag', 'php');
$attributes->put('com.threecrickets.prudence.DelegatedResource.defaultName', $resourcesDefaultName);
$attributes->put('com.threecrickets.prudence.DelegatedResource.documentSource', $resourcesDocumentSource);
$attributes->put('com.threecrickets.prudence.DelegatedResource.sourceViewable', $resourcesSourceViewable);

$resources = new Finder($application->context, $classLoader->loadClass('com.threecrickets.prudence.DelegatedResource'));
$router->attachBase(fixURL($resourcesBaseURL), $resources);

if($resourcesDefrost) {
	$defrostTasks = DefrostTask::forDocumentSource($resourcesDocumentSource, $languageManager, false, true);
	foreach($defrostTasks as $defrostTask) {
		array_push($tasks, $defrostTask);
	}
}

//
// SourceCode
//

if(showDebugOnError) {
	$documentSources = new ArrayList();
	$documentSources->add($dynamicWebDocumentSource);
	$documentSources->add($resourcesDocumentSource);
	$attributes->put('com.threecrickets.prudence.SourceCodeResource.documentSources', $documentSources);
	$sourceCode = new Finder($application->context, $classLoader->loadClass('com.threecrickets.prudence.SourceCodeResource'));
	$router->attach(fixURL($showSourceCodeURL), $sourceCode)->matchingMode = Template::MODE_EQUALS;
}

//
// Preheat
//

if($dynamicWebPreheat) {
	$preheatTasks = PreheatTask::forDocumentSource($dynamicWebDocumentSource, $component->context, $applicationInternalName);
	foreach($preheatTasks as $preheatTask) {
		array_push($tasks, $preheatTask);
	}
}

foreach($preheatResources as $preheatResource) {
	array_push($tasks, new PreheatTask($component->context, $applicationInternalName, $preheatResource));
}
?>