//
// Prudence Application
//

importClass(
	java.io.File,
	java.io.FileNotFoundException,
	javax.script.ScriptEngineManager,
	org.restlet.Application,
	org.restlet.data.Reference,
	org.restlet.data.MediaType,
	com.threecrickets.scripturian.file.DocumentFileSource);

//
// Settings
//

includeOrDefault(applicationBasePath + '/settings', 'defaults/application/settings');

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
// MetaData
//

application.metadataService.addExtension('php', MediaType.TEXT_HTML);

//
// Routing
//

includeOrDefault(applicationBasePath + '/routing', 'defaults/application/routing');

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

attributes.put('com.threecrickets.prudence.DelegatedResource.engineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourceDefaultName);
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource',
	new DocumentFileSource(new File(applicationBasePath + resourceBasePath), resourceDefaultName, resourceMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourceSourceViewable);

// GeneratedTextResource

attributes.put('com.threecrickets.prudence.GeneratedTextResource.engineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',
	 new DocumentFileSource(new File(applicationBasePath + dynamicWebBasePath), dynamicWebDefaultDocument, dynamicWebMinimumTimeBetweenValidityChecks));
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable);

// Additional runtime attributes

for(var key in runtimeAttributes) {
	attributes.put(key, runtimeAttributes[key]);
}
