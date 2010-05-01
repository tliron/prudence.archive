<?php
//
// Prudence Application
//

global $application, $application_base_path, $attributes;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $show_debug_on_error, $show_source_code_url;
global $application_logger_name;
global $runtime_attributes;

import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import com.threecrickets.prudence.util.DelegatedStatusService;

//
// Settings
//

include_or_default($application_base_path . '/settings/', 'defaults/application/settings/');

//
// Application
//

include_or_default($application_base_path . '/application/', 'defaults/application/application/');

$application->name = $application_name;
$application->description = $application_description;
$application->author = $application_author;
$application->owner = $application_owner;

//
// StatusService
//

$application->statusService = new DelegatedStatusService($show_debug_on_error ? $show_source_code_url : null);
$application->statusService->debugging = $show_debug_on_error;
$application->statusService->homeRef = new Reference($application_home_url);
$application->statusService->contactEmail = $application_contact_email;

//
// MetaData
//

$application->metadataService->addExtension('php', MediaType::valueOf('text/html'));

//
// Routing
//

include_or_default($application_base_path . '/routing/', 'defaults/application/routing/');

//
// Logging
//

$application->context->setLogger($application_logger_name);

//
// Additional/Override Runtime Attributes
//

foreach($runtime_attributes as $key => $value) {
	$attributes[$key] = $value;
}
?>