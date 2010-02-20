//
// Prudence Component
//

importClass(
	java.lang.System,
	java.lang.Runtime,
	java.io.FileNotFoundException,
	java.util.logging.LogManager,
	java.util.concurrent.Executors,
	org.restlet.Component,
	com.threecrickets.prudence.util.DelegatedStatusService,
	com.threecrickets.prudence.util.MessageTask);

function includeOrDefault(name, def) {
	try {
		document.container.include(name);
	} catch(e if e.javaException instanceof FileNotFoundException) {
		if(!def) {
			def = 'defaults/' + name;
		}
		document.container.include(def);
	}
}

var tasks = [];

//
// Welcome
//

print('Prudence 1.0 for JavaScript.\n');

//
// Component
//

var component = new Component();

//
// Logging
//

// log4j: This is our actual logging engine
try {
	importClass(org.apache.log4j.PropertyConfigurator);
	PropertyConfigurator.configure('conf/logging.conf');
} catch(x) {}

// JULI: Remove any pre-existing configuration
LogManager.logManager.reset();

// JULI: Bridge to SLF4J, which will use log4j as its engine 
try {
	importClass(org.slf4j.bridge.SLF4JBridgeHandler);
	SLF4JBridgeHandler.install();
} catch(x) {}

// Set Restlet to use SLF4J, which will use log4j as its engine
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade');

// Velocity logging
System.setProperty('com.sun.script.velocity.properties', 'conf/velocity.conf');

// Web requests
component.logService.loggerName = 'web-requests';

//
// StatusService
//

component.statusService = new DelegatedStatusService();

//
// Executor
//

var executor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors());
component.context.attributes.put('executor', executor);

//
// Clients
//

includeOrDefault('instance/clients');

//
// Routing
//

includeOrDefault('instance/routing');

//
// Servers
//

includeOrDefault('instance/servers');

//
// Start
//

component.start();

//
// Tasks
//

if(tasks.length > 0) {
	executor.submit(new MessageTask(component.context, 'Executing ' + tasks.length + ' tasks...'));
	for(var i in tasks) {
		var task = tasks[i];
		executor.submit(task);
	}
	executor.submit(new MessageTask(component.context, 'Finished tasks.'));
}