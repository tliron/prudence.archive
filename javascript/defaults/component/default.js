//
// Prudence Component
//

importClass(
	java.lang.System,
	java.io.File,
	java.io.FileNotFoundException,
	java.util.logging.LogManager,
	org.restlet.Component,
	org.restlet.data.Protocol);

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
// Clients
//

includeOrDefault('component/clients');

//
// Hosts
//

includeOrDefault('component/hosts');

//
// Applications
//

var applications = [];
component.context.attributes.put('applications', applications);
var applicationDirs = new File('applications').listFiles();
for(var i in applicationDirs) {
	var applicationDir = applicationDirs[i]; 
	if(applicationDir.isDirectory()) {
		var applicationName = applicationDir.name;
		var applicationLoggerName = applicationDir.name;
		var applicationBasePath = applicationDir.path;
		var applicationDefaultURL = '/' + applicationDir.name + '/';
		includeOrDefault(applicationBasePath, 'defaults/application');
		applications[applications.length] = application;
	}
}

if(applications.length == 0) {
	print('No applications found. Exiting.\n');
	System.exit(0);
}

//
// Servers
//

includeOrDefault('component/servers');

//
// Start
//

component.start();
