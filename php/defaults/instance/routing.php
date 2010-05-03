<?php
//
// Prudence Routing
//

global $component, $application_name, $application_internal_name, $application_logger_name, $application_base_path, $application_default_url, $application;

import java.io.File;
import java.util.ArrayList;

// Hosts

execute_or_default('instance/hosts/');

// Applications

$applications = new ArrayList();
$component->context->attributes['applications'] = $applications;
$application_dirs = new File('applications')->listFiles();
foreach($application_dirs as $application_dir) {
	if($application_dir->directory) {
		$application_name = $application_dir->name;
		$application_internal_name = $application_dir->name;
		$application_logger_name = $application_dir->name;
		$application_base_path = $application_dir->path;
		$application_default_url = '/' . $application_dir->name . '/';
		execute_or_default($application_base_path, 'defaults/application');
		$applications->add($application);
	}
}

if($applications->empty) {
	print "No applications found. Exiting.\n";
	flush();
	$executable->context->writer->flush();
	System::exit(0);
}
?>