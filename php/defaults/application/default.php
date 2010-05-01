<?php
//
// Prudence Application
//

global $application, $applicationBasePath, $attributes;
global $applicationName, $applicationDescription, $applicationAuthor, $applicationHomeURL, $applicationContactEmail;
global $showDebugOnError, $showSourceCodeURL;
global $applicationLoggerName;
global $runtimeAttributes;

import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import com.threecrickets.prudence.util.DelegatedStatusService;

//
// Settings
//

includeOrDefault($applicationBasePath . '/settings/', 'defaults/application/settings/');

//
// Application
//

includeOrDefault($applicationBasePath . '/application/', 'defaults/application/application/');

$application->name = $applicationName;
$application->description = $applicationDescription;
$application->author = $applicationAuthor;
$application->owner = $applicationOwner;

//
// StatusService
//

$application->statusService = new DelegatedStatusService($showDebugOnError ? $showSourceCodeURL : null);
$application->statusService->debugging = $showDebugOnError;
$application->statusService->homeRef = new Reference($applicationHomeURL);
$application->statusService->contactEmail = $applicationContactEmail;

//
// MetaData
//

$application->metadataService->addExtension('php', MediaType::valueOf('text/html'));

//
// Routing
//

includeOrDefault($applicationBasePath . '/routing/', 'defaults/application/routing/');

//
// Logging
//

$application->context->setLogger($applicationLoggerName);

//
// Additional/Override Runtime Attributes
//

foreach($runtimeAttributes as $key => $value) {
	$attributes->put($key, $value);
}
?>