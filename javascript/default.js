//
// Prudence Component
//

importClass(
	java.lang.System,
	java.io.File,
	java.util.logging.LogManager,
	org.restlet.Component,
	org.restlet.data.Protocol);

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

// Required for use of Directory
component.clients.add(Protocol.FILE);

//
// Servers
//

document.container.include('conf/servers');

//
// Hosts
//

document.container.include('conf/hosts');

//
// Applications
//

var applicationBasePath = 'applications/prudence-test';
var applicationBaseURL ='/prudence-test';
document.container.include(applicationBasePath);

//
// Start
//

component.start();

