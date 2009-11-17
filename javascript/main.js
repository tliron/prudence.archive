<%

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

document.container.include('conf/prudence.conf');

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

// Directory
directory = new Directory(application.context, File(staticWebBasePath).toURI().toString());
directory.listingAllowed = staticWebDirectoryListingAllowed;
directory.negotiateContent = true;

// Redirect to trailing slashes
for(var i in urlAddTrailingSlash) {
	if(urlAddTrailingSlash[i].slice(-1) == '/') {
		urlAddTrailingSlash[i] = urlAddTrailingSlash[i].slice(0, -1);
	}
	var redirector = new Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER);
	router.attach(urlAddTrailingSlash[i], redirector).matchingMode = Template.MODE_EQUALS;
}

// Note that order of attachment is important -- first matching pattern wins
classLoader = ClassLoader.systemClassLoader;
router.attach(staticWebBaseURL, directory).matchingMode = Template.MODE_STARTS_WITH;
router.attach(resourceBaseURL, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH;
router.attach(dynamicWebBaseURL, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH;

application.inboundRoot = router

//
// Attributes
//

var attributes = application.context.attributes;
var scriptEngineManager = new ScriptEngineManager();

// DelegatedResource

attributes.put('com.threecrickets.prudence.DelegatedResource.scriptEngineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName', document.container.defaultEngineName);
attributes.put('com.threecrickets.prudence.DelegatedResource.extension', resourceExtension);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourceDefaultName);
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource',
	new DocumentFileSource(new File(resourceBasePath), resourceDefaultName, resourceExtension, resourceMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourceSourceViewable);

// GeneratedTextResource

attributes.put('com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName', document.container.defaultEngineName);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',
	 new DocumentFileSource(new File(dynamicWebBasePath), dynamicWebDefaultDocument, null, dynamicWebMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable);

//
// Start
//

component.start();

print('Prudence started at port ' + serverPort);
%>