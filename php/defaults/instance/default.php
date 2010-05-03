<?php
//
// Prudence Component
//

global $tasks;

import java.lang.System;
import java.lang.Runtime;
import java.io.FileNotFoundException;
import java.util.logging.LogManager;
import java.util.concurrent.Executors;
import org.restlet.Component;
import com.threecrickets.prudence.util.DelegatedStatusService;
import com.threecrickets.prudence.util.MessageTask;

function execute_or_default($name, $def=NULL) {
	global $executable;
	try {
		$executable->container->execute($name);
	} catch(Exception $x) {
		if(is_null($def)) {
			$def = 'defaults/' . $name;
		}
		$executable->container->execute($def);
	}
}

$tasks = array();

//
// Version
//

$prudence_version = '1.0';
$prudence_revision = '-%REVISION%';
if(strlen($prudence_revision) == 1) {
	$prudence_revision = '';
}
$prudence_flavor = 'PHP';

//
// Welcome
//

print 'Prudence ' . $prudence_version . $prudence_revision . ' for ' . $prudence_flavor . ".\n";

//
// Component
//

$component = new Component();
$component->context->attributes['prudence.version'] = $prudence_version;
$component->context->attributes['prudence.revision'] = $prudence_revision;
$component->context->attributes['prudence.flavor'] = $prudence_flavor;

//
// Logging
//

// log4j: This is our actual logging engine
try {
	import org.apache.log4j.PropertyConfigurator;
	PropertyConfigurator::configure('configuration/logging.conf');
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

// Velocity logging
System::setProperty('com.sun.script.velocity.properties', 'configuration/velocity.conf');

// Web requests
$component->logService->loggerName = 'web-requests';

//
// StatusService
//

$component->statusService = new DelegatedStatusService();

//
// Executor
//

$executor = Executors::newFixedThreadPool(Runtime::getRuntime()->availableProcessors());
$component->context->attributes['prudence.executor'] = $executor;

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
// Start
//

$component->start();

//
// Tasks
//

if(count($tasks) > 0) {
	$executor->submit(new MessageTask($component->context, 'Executing ' . count($tasks) . ' tasks...'));
	foreach($tasks as $task) {
		$executor->submit($task);
	}
	$executor->submit(new MessageTask($component->context, 'Finished tasks.'));
}
?>