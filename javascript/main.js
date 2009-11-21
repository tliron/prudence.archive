
importClass(
	java.lang.System,
	java.lang.ClassLoader,
	java.io.File,
	java.util.logging.LogManager,
	javax.script.ScriptEngineManager,
	org.restlet.Application,
	org.restlet.Component,
	org.restlet.resource.Directory,
	org.restlet.routing.Router,
	org.restlet.routing.Redirector,
	org.restlet.routing.Template,
	org.restlet.data.Protocol,
	org.restlet.data.Reference,
	com.threecrickets.prudence.DelegatedResource,
	com.threecrickets.prudence.GeneratedTextResource,
	com.threecrickets.scripturian.file.DocumentFileSource);

document.container.include('conf/prudence.js');

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

//
// Component and Server
//

var component = new Component();
component.servers.add(Protocol.HTTP, serverPort);
component.clients.add(Protocol.FILE);
component.logService.loggerName = componentWebLoggerName;

//
// Application
//

application = new Application()
application.name = applicationName;
application.description = applicationDescription;
application.author = applicationAuthor;
application.owner = applicationOwner;
application.statusService.homeRef = new Reference(applicationHomeURL);
application.statusService.contactEmail = applicationContactEmail;

// The context will not be created until we attach the application
component.defaultHost.attach(application);

application.context.logger = applicationLoggerName;

//
// Router
//

router = new Router(application.context)
application.inboundRoot = router
document.container.include('conf/routing.js');

//
// Attributes
//

var attributes = application.context.attributes;
var scriptEngineManager = new ScriptEngineManager();

// DelegatedResource

attributes.put('com.threecrickets.prudence.DelegatedResource.scriptEngineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.DelegatedResource.extension', resourceExtension);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourceDefaultName);
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource',
	new DocumentFileSource(new File(resourceBasePath), resourceDefaultName, resourceExtension, resourceMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourceSourceViewable);

// GeneratedTextResource

attributes.put('com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',
	 new DocumentFileSource(new File(dynamicWebBasePath), dynamicWebDefaultDocument, dynamicWebExtension, dynamicWebMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable);

//
// Start
//

component.start();

print('Prudence started at port ' + serverPort + '\n');
