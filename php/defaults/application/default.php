<?php
//
// Prudence Application
//

global $application_instance, $application_base_path, $attributes;
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

execute_or_default($application_base_path . '/settings/', 'defaults/application/settings/');

//
// Application
//

execute_or_default($application_base_path . '/application/', 'defaults/application/application/');

$application_instance->name = $application_name;
$application_instance->description = $application_description;
$application_instance->author = $application_author;
$application_instance->owner = $application_owner;

//
// StatusService
//

$application_instance->statusService = new DelegatedStatusService($show_debug_on_error ? $show_source_code_url : null);
$application_instance->statusService->debugging = $show_debug_on_error;
$application_instance->statusService->homeRef = new Reference($application_home_url);
$application_instance->statusService->contactEmail = $application_contact_email;

//
// MetaData
//

$application_instance->metadataService->addExtension('php', MediaType::valueOf('text/html'));

//
// Routing
//

execute_or_default($application_base_path . '/routing/', 'defaults/application/routing/');

//
// Logging
//

$application_instance->context->setLogger($application_logger_name);

//
// Additional/Override Runtime Attributes
//

foreach($runtime_attributes as $key => $value) {
	$attributes[$key] = $value;
}
?>