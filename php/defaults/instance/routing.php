<?php
//
// Prudence Routing
//

global $component, $applicationName, $applicationInternalName, $applicationLoggerName, $applicationBasePath, $applicationDefaultURL, $application;

import java.io.File;
import java.util.ArrayList;

// Hosts

includeOrDefault('instance/hosts/');

// Applications

$applications = new ArrayList();
$component->context->attributes->put('applications', $applications);
$applicationDirs = new File('applications')->listFiles();
foreach($applicationDirs as $applicationDir) {
	if($applicationDir->directory) {
		$applicationName = $applicationDir->name;
		$applicationInternalName = $applicationDir->name;
		$applicationLoggerName = $applicationDir->name;
		$applicationBasePath = $applicationDir->path;
		$applicationDefaultURL = '/' . $applicationDir->name . '/';
		includeOrDefault($applicationBasePath, 'defaults/application');
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