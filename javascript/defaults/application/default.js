//
// Prudence Application
//

importClass(
	org.restlet.data.Reference,
	org.restlet.data.MediaType,
	com.threecrickets.prudence.util.DelegatedStatusService,
	com.threecrickets.prudence.util.PrudenceCronTaskCollector)

//
// Settings
//

executeOrDefault(applicationBasePath + '/settings/', 'defaults/application/settings/')

//
// Application
//

executeOrDefault(applicationBasePath + '/application/', 'defaults/application/application/')

applicationInstance.name = applicationName
applicationInstance.description = applicationDescription
applicationInstance.author = applicationAuthor
applicationInstance.owner = applicationOwner

//
// StatusService
//

applicationInstance.statusService = new DelegatedStatusService(showDebugOnError ? showSourceCodeURL : null)
applicationInstance.statusService.debugging = showDebugOnError
applicationInstance.statusService.homeRef = new Reference(applicationHomeURL)
applicationInstance.statusService.contactEmail = applicationContactEmail

//
// MetaData
//

applicationInstance.metadataService.addExtension('php', MediaType.TEXT_HTML)

//
// Routing
//

executeOrDefault(applicationBasePath + '/routing/', 'defaults/application/routing/')

//
// Logging
//

applicationInstance.context.setLogger(applicationLoggerName)

//
// Predefined Globals
//

for(var key in predefinedGlobals) {
	attributes.put(key, predefinedGlobals[key])
}

//
// Tasks
//

var schedulerDocumentSource = new DocumentFileSource(applicationBasePath + tasksBasePath, tasksDefaultDocument, 'js', tasksMinimumTimeBetweenValidityChecks)
var taskCollector = new PrudenceCronTaskCollector(new File(applicationBasePath + '/crontab'), schedulerDocumentSource, languageManager, 'javascript', true, applicationInstance.context)
scheduler.addTaskCollector(taskCollector)
