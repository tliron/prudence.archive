//
// Prudence Application
//
// Copyright 2009-2010 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

importClass(
	org.restlet.data.Reference,
	org.restlet.data.MediaType,
	com.threecrickets.prudence.DelegatedStatusService,
	com.threecrickets.prudence.ApplicationTaskCollector)

//
// Settings
//

executeOrDefault(applicationBase + '/settings/', 'defaults/application/settings/')

//
// Application
//

executeOrDefault(applicationBase + '/application/', 'defaults/application/application/')

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

executeOrDefault(applicationBase + '/routing/', 'defaults/application/routing/')

//
// Logging
//

applicationInstance.context.setLogger(applicationLoggerName)

//
// Predefined Globals
//

for(var key in predefinedGlobals) {
	applicationGlobals.put(key, predefinedGlobals[key])
}

//
// Tasks
//

var tasksDocumentSource = new DocumentFileSource(applicationBase + tasksBasePath, applicationBasePath + tasksBasePath, tasksDefaultName, 'js', tasksMinimumTimeBetweenValidityChecks)
applicationGlobals.put('com.threecrickets.prudence.ApplicationTask.languageManager', languageManager)
applicationGlobals.put('com.threecrickets.prudence.ApplicationTask.defaultLanguageTag', 'javascript')
applicationGlobals.put('com.threecrickets.prudence.ApplicationTask.defaultName', tasksDefaultName)
applicationGlobals.put('com.threecrickets.prudence.ApplicationTask.documentSource', tasksDocumentSource)
scheduler.addTaskCollector(new ApplicationTaskCollector(new File(applicationBasePath + '/crontab'), applicationInstance))
