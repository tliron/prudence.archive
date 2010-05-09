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

applicationInstance.name = applicationName;
applicationInstance.description = applicationDescription;
applicationInstance.author = applicationAuthor;
applicationInstance.owner = applicationOwner;

//
// StatusService
//

applicationInstance.statusService = new DelegatedStatusService(showDebugOnError ? showSourceCodeURL : null);
applicationInstance.statusService.debugging = showDebugOnError;
applicationInstance.statusService.homeRef = new Reference(applicationHomeURL);
applicationInstance.statusService.contactEmail = applicationContactEmail;

//
// MetaData
//

applicationInstance.metadataService.addExtension('php', MediaType.TEXT_HTML);

//
// Routing
//

executeOrDefault(applicationBasePath + '/routing/', 'defaults/application/routing/');

//
// Logging
//

applicationInstance.context.setLogger(applicationLoggerName);

//
// Additional/Override Runtime Attributes
//

for(var key in runtimeAttributes) {
	attributes.put(key, runtimeAttributes[key]);
}
