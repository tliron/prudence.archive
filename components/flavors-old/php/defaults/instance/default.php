<?php
//
// Prudence Instance
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

global $tasks, $scheduler, $component, $prudence_version, $prudence_flavor, $applications, $predefined_shared_globals;
global $common_libraries_document_source, $common_fragments_document_source, $common_tasks_document_sources, $common_handlers_document_sources;

import java.lang.System;
import java.util.logging.LogManager;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.File;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.prudence.service.ApplicationService;

//
// Common
//

$common_libraries_document_source = new DocumentFileSource('common/libraries/', new File($document->source->basePath, 'common/libraries/'), 'default', 'php', 5000);
$common_fragments_document_source = new DocumentFileSource('common/web/fragments/', new File($document->source->basePath, 'common/web/fragments/'), 'index', 'php', 5000);

$common_tasks_document_sources = new CopyOnWriteArrayList();
$common_tasks_document_sources->add(new DocumentFileSource('common/tasks/', new File($document->source->basePath, 'common/tasks/'), 'default', 'php', 5000));
$common_handlers_document_sources = new CopyOnWriteArrayList();
$common_handlers_document_sources->add(new DocumentFileSource('common/handlers/', new File($document->source->basePath, 'common/handlers/'), 'default', 'php', 5000));

$document->librarySources->add($common_libraries_document_source);

//
// Utilities
//

function is_java_exception($x, $name) {
	$name = $name . ':';
	if($x->__javaException) {
		return substr($x->__javaException, 0, strlen($name)) == $name;
	}
	return FALSE;
}

function execute_or_default($name, $def=NULL) {
	global $document;
	try {
		$document->execute($name);
	}
	catch(Exception $x) {
		if(is_java_exception($x, 'com.threecrickets.scripturian.exception.DocumentNotFoundException')) {
			if(is_null($def)) {
				$def = '/defaults/' . $name;
			}
			$document->execute($def);
		}
		else {
			if($x->__javaException) {
				$x->__javaException->printStackTrace();
			} else {
				print $x->getMessage() . "\n";
			}
			throw $x;
		}
	}
}

//
// Version
//

$prudence_version = '%VERSION%';
$prudence_flavor = 'PHP';

//
// Welcome
//

print 'Prudence ' . $prudence_version . ' for ' . $prudence_flavor . ".\n";

//
// Logging
//

// log4j: This is our actual logging engine
try {
	import org.apache.log4j.PropertyConfigurator;
	System::setProperty('prudence.logs', $document->source->basePath->path . '/logs');
	PropertyConfigurator::configure($document->source->basePath->path . '/configuration/logging.conf');
} catch(Exception $x) {}

// JULI: Remove any pre-existing configuration
LogManager::getlogManager()->reset();

// JULI: Bridge to SLF4J, which will use log4j as its engine 
try {
	import org.slf4j.bridge.SLF4JBridgeHandler;
	SLF4JBridgeHandler::install();
} catch(Exception $x) {}

// Set Restlet to use SLF4J, which will use log4j as its engine
System::setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade');

// Set Velocity to use log4j
$executable->manager->attributes['velocity.runtime.log.logsystem.class'] = 'org.apache.velocity.runtime.log.Log4JLogChute';
$executable->manager->attributes['velocity.runtime.log.logsystem.log4j.logger'] = 'velocity';

// Set spymemcached to use log4j
System::setProperty('net.spy.log.LoggerImpl', 'net.spy.memcached.compat.log.Log4JLogger');

//
// Configuration
//

// Hazelcast
System::setProperty('hazelcast.config', 'configuration/hazelcast.conf');


//
// Component
//

execute_or_default('instance/component/');

//
// Clients
//

execute_or_default('instance/clients/');

//
// Routing
//

execute_or_default('instance/routing/');

//
// Servers
//

execute_or_default('instance/servers/');

//
// Predefined Shared Globals
//

foreach($predefined_shared_globals as $key => $value) {
	$component->context->attributes[$key] = $value;
}

//
// Start
//

$component->start();

print "Prudence is up!\n";
for($i = 0; $i < $component->servers->size(); $i++) {
	$server = $component->servers->get($i);
	if($server->address) {
		print 'Listening on ' . $server->address . ' port ' . $server->port . ' for ';
	} else {
		print 'Listening on port ' . $server->port . ' for ';
	}
	for($j = 0; $j < $server->protocols->size(); $j++) {
		$protocol = $server->protocols->get($j);
		if($j < $server->protocols->size() - 1) {
			print ', ';
		}
		print($protocol);
	}
	print ".\n";
}

//
// Scheduler
//

$scheduler->start();

//
// Tasks
//

$fixed_executor = Executors::newFixedThreadPool(Runtime::getRuntime()->availableProcessors() * 2 + 1);
if(count($tasks) > 0) {
	$futures = array();
	$start_time = System::currentTimeMillis();
	print 'Executing ' . count($tasks) . " startup tasks...\n";
	foreach($tasks as $task) {
		$futures[] = $fixed_executor->submit($task);
	}
	foreach($futures as $future) {
		try {
			$future->get();
		}
		catch(Exception $x) {
		}
	}
	print 'Finished all startup tasks in ' . ((System::currentTimeMillis() - $start_time) / 1000) . " seconds.\n";
}

for($i = 0; $i < $applications->size(); $i++) {
	$application_service = new ApplicationService($applications->get($i));
	$application_service->task(NULL, '/startup/', NULL, 'initial', 0, 0, FALSE);
}
?>