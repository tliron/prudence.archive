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

function includeOrDefault($name, $def=NULL) {
	global $executable;
	try {
		$executable->container->include($name);
	} catch(Exception $x) {
		if(is_null($def)) {
			$def = 'defaults/' . $name;
		}
		$executable->container->include($def);
	}
}

$tasks = array();

//
// Version
//

$prudenceVersion = '1.0';
$prudenceRevision = '-%REVISION%';
if(strlen($prudenceRevision) == 1) {
	$prudenceRevision = '';
}
$prudenceFlavor = 'PHP';

//
// Welcome
//

print 'Prudence ' . $prudenceVersion . $prudenceRevision . ' for ' . $prudenceFlavor . ".\n";

//
// Component
//

$component = new Component();
$component->context->attributes->put('prudence.version', prudenceVersion);
$component->context->attributes->put('prudence.revision', prudenceRevision);
$component->context->attributes->put('prudence.flavor', prudenceFlavor);

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
$component->context->attributes->put('prudence.executor', $executor);

//
// Clients
//

includeOrDefault('instance/clients/');

//
// Routing
//

includeOrDefault('instance/routing/');

//
// Servers
//

includeOrDefault('instance/servers/');

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