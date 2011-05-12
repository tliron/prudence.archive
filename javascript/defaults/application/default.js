//
// Prudence Application
//
// Copyright 2009-2011 Three Crickets LLC.
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
	com.threecrickets.prudence.ApplicationTaskCollector,
	com.threecrickets.prudence.util.LoggingUtil,
	com.threecrickets.prudence.service.ApplicationService)

//
// Settings
//

executeOrDefault(applicationBase + '/settings/', '/defaults/application/settings/')

//
// Application
//

executeOrDefault(applicationBase + '/application/', '/defaults/application/application/')

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

executeOrDefault(applicationBase + '/routing/', '/defaults/application/routing/')

//
// Logging
//

applicationInstance.context.logger = LoggingUtil.getRestletLogger(applicationLoggerName)

//
// Predefined Globals
//

for(var key in predefinedGlobals) {
	applicationGlobals.put(key, predefinedGlobals[key])
}

//
// Handlers
//

var handlersDocumentSource = new DocumentFileSource(applicationBase + handlersBasePath, applicationBasePath + handlersBasePath, documentsDefaultName, 'js', minimumTimeBetweenValidityChecks)
applicationGlobals.put('com.threecrickets.prudence.DelegatedHandler.documentSource', handlersDocumentSource)
applicationGlobals.put('com.threecrickets.prudence.DelegatedHandler.extraDocumentSources', commonHandlersDocumentSources)

//
// Tasks
//

var tasksDocumentSource = new DocumentFileSource(applicationBase + tasksBasePath, applicationBasePath + tasksBasePath, documentsDefaultName, 'js', minimumTimeBetweenValidityChecks)
applicationGlobals.put('com.threecrickets.prudence.ApplicationTask.documentSource', tasksDocumentSource)
applicationGlobals.put('com.threecrickets.prudence.ApplicationTask.extraDocumentSources', commonTasksDocumentSources)

scheduler.addTaskCollector(new ApplicationTaskCollector(new File(applicationBasePath + '/crontab'), applicationInstance))

//
// Common Configurations
//

var fileUploadDirectory = new File(applicationBasePath + fileUploadBasePath)
function configureCommon(prefix) {
	applicationGlobals.put(prefix + '.languageManager', languageManager)
	applicationGlobals.put(prefix + '.defaultName', documentsDefaultName)
	applicationGlobals.put(prefix + '.defaultLanguageTag', 'javascript')
	applicationGlobals.put(prefix + '.libraryDocumentSources', librariesDocumentSources)
	applicationGlobals.put(prefix + '.fileUploadDirectory', fileUploadDirectory)
	applicationGlobals.put(prefix + '.fileUploadSizeThreshold', fileUploadSizeThreshold)
	applicationGlobals.put(prefix + '.sourceViewable', sourceViewable)
}

configureCommon('com.threecrickets.prudence.GeneratedTextResource')
configureCommon('com.threecrickets.prudence.DelegatedResource')
configureCommon('com.threecrickets.prudence.DelegatedHandler')
configureCommon('com.threecrickets.prudence.ApplicationTask')

//
// ApplicationService
//

var applicationService = new ApplicationService(applicationInstance)
