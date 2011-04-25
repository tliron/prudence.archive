<?php
//
// Prudence Routing
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

global $component, $application_name, $application_internal_name, $application_logger_name, $application_base_path, $application_default_url, $application_base, $application_instance, $applications;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import com.threecrickets.prudence.util.IoUtil;

// Hosts

execute_or_default('instance/hosts/');

// Unzip

$common_dir = new File($document->source->basePath, 'common');
$properties_file = new File($common_dir, 'common.properties');
$properties = IoUtil::loadProperties($properties_file);
$save_properties = FALSE;
$common_files = $common_dir->listFiles();
foreach($common_files as $common_file) {
	if(!$common_file->directory && substr($common_file->name, -4) == '.zip' && $properties->getProperty($common_file->name, '') != $common_file->lastModified()) {
		print 'Unpacking common "' . $common_file->name . '"...' . "\n";
		IoUtil::unzip($common_file, $common_dir);
		$properties->setProperty($common_file->name, $common_file->lastModified());
		$save_properties = TRUE;
	}
}
if($save_properties) {
	IoUtil::saveProperties($properties, $properties_file);
}

$applications_dir = new File($document->source->basePath, 'applications');
$properties_file = new File($applications_dir, 'applications.properties');
$properties = IoUtil::loadProperties($properties_file);
$save_properties = FALSE;
$applications_files = $applications_dir->listFiles();
foreach($applications_files as $applications_file) {
	if(!$applications_file->directory && substr($applications_file->name, -4) == '.zip' && $properties->getProperty($applications_file->name, '') != $applications_file->lastModified()) {
		print 'Unpacking applications "' . $applications_file->name . '"...' . "\n";
		IoUtil::unzip($applications_file, $applications_dir);
		$properties->setProperty($applications_file->name, $applications_file->lastModified());
		$save_properties = TRUE;
	}
}
if($save_properties) {
	IoUtil::saveProperties($properties, $properties_file);
}

// Applications

$applications = new CopyOnWriteArrayList();
$component->context->attributes['com.threecrickets.prudence.applications'] = $applications;

$application_dirs = $applications_dir->listFiles();
foreach($application_dirs as $application_dir) {
	if($application_dir->directory && !$application_dir->hidden) {
		$application_name = $application_dir->name;
		$application_internal_name = $application_dir->name;
		$application_logger_name = $application_dir->name;
		$application_base_path = $application_dir->path;
		$application_default_url = '/' . $application_dir->name;
		$application_base = 'applications/' . $application_dir->name;
		execute_or_default($application_base, 'defaults/application/');
		$applications->add($application_instance);
	}
}

if($applications->empty) {
	print "No applications found. Exiting.\n";
	flush();
	$executable->context->writer->flush();
	System::exit(0);
}
?>