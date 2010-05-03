//
// Prudence Application
//

importClass(
	org.restlet.data.Reference,
	org.restlet.data.MediaType,
	com.threecrickets.prudence.util.DelegatedStatusService);

//
// Settings
//

executeOrDefault(applicationBasePath + '/settings/', 'defaults/application/settings/');

//
// Application
//

executeOrDefault(applicationBasePath + '/application/', 'defaults/application/application/');

application.name = applicationName;
application.description = applicationDescription;
application.author = applicationAuthor;
application.owner = applicationOwner;

//
// StatusService
//

application.statusService = new DelegatedStatusService(showDebugOnError ? showSourceCodeURL : null);
application.statusService.debugging = showDebugOnError;
application.statusService.homeRef = new Reference(applicationHomeURL);
application.statusService.contactEmail = applicationContactEmail;

//
// MetaData
//

application.metadataService.addExtension('php', MediaType.TEXT_HTML);

//
// Routing
//

executeOrDefault(applicationBasePath + '/routing/', 'defaults/application/routing/');

//
// Logging
//

application.context.setLogger(applicationLoggerName);

//
// Additional/Override Runtime Attributes
//

for(var key in runtimeAttributes) {
	attributes.put(key, runtimeAttributes[key]);
}
