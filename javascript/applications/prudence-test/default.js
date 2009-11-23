//
// Prudence Application
//

importClass(
	java.io.File,
	javax.script.ScriptEngineManager,
	org.restlet.Application,
	org.restlet.data.Reference,
	com.threecrickets.scripturian.file.DocumentFileSource);

document.container.include(applicationBasePath + '/conf/prudence');

//
// Application
//

var application = new Application();
application.name = applicationName;
application.description = applicationDescription;
application.author = applicationAuthor;
application.owner = applicationOwner;
application.statusService.homeRef = new Reference(applicationHomeURL);
application.statusService.contactEmail = applicationContactEmail;

//
// Routing
//

document.container.include(applicationBasePath + '/conf/routing');

//
// Logging
//

application.context.setLogger(applicationLoggerName);

//
// Configuration
//

var attributes = application.context.attributes;

scriptEngineManager = new ScriptEngineManager();

// DelegatedResource

attributes.put('com.threecrickets.prudence.DelegatedResource.scriptEngineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.DelegatedResource.extension', resourceExtension);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourceDefaultName);
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource',
	new DocumentFileSource(new File(applicationBasePath + resourceBasePath), resourceDefaultName, resourceExtension, resourceMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourceSourceViewable);

// GeneratedTextResource

attributes.put('com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',
	 new DocumentFileSource(new File(applicationBasePath + dynamicWebBasePath), dynamicWebDefaultDocument, dynamicWebExtension, dynamicWebMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable);
