//
// Prudence Application
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

import org.restlet.data.Reference
import org.restlet.data.MediaType
import com.threecrickets.scripturian.document.DocumentFileSource
import com.threecrickets.prudence.DelegatedStatusService
import com.threecrickets.prudence.ApplicationTaskCollector
import com.threecrickets.prudence.util.LoggingUtil
import com.threecrickets.prudence.service.ApplicationService

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

predefinedGlobals.each() { key, value ->
	applicationGlobals[key] = value
}


//
// Handlers
//

handlersDocumentSource = new DocumentFileSource(applicationBase + handlersBasePath, applicationBasePath + handlersBasePath, documentsDefaultName, 'groovy', minimumTimeBetweenValidityChecks)
applicationGlobals['com.threecrickets.prudence.DelegatedHandler.documentSource'] = handlersDocumentSource
applicationGlobals['com.threecrickets.prudence.DelegatedHandler.extraDocumentSources'] = commonHandlersDocumentSources

//
// Tasks
//

tasksDocumentSource = new DocumentFileSource(applicationBase + tasksBasePath, applicationBasePath + tasksBasePath, documentsDefaultName, 'groovy', minimumTimeBetweenValidityChecks)
applicationGlobals['com.threecrickets.prudence.ApplicationTask.documentSource'] = tasksDocumentSource
applicationGlobals['com.threecrickets.prudence.ApplicationTask.extraDocumentSources'] = commonTasksDocumentSources

scheduler.addTaskCollector(new ApplicationTaskCollector(new File(applicationBasePath + '/crontab'), applicationInstance))

//
// Common Configurations
//

fileUploadDirectory = new File(applicationBasePath + fileUploadBasePath)
configureCommon = { prefix ->
	applicationGlobals[prefix + '.languageManager'] = languageManager
	applicationGlobals[prefix + '.defaultName'] = documentsDefaultName
	applicationGlobals[prefix + '.defaultLanguageTag'] = 'groovy'
	applicationGlobals[prefix + '.libraryDocumentSources'] = librariesDocumentSources
	applicationGlobals[prefix + '.fileUploadDirectory'] = fileUploadDirectory
	applicationGlobals[prefix + '.fileUploadSizeThreshold'] = fileUploadSizeThreshold
	applicationGlobals[prefix + '.sourceViewable'] = sourceViewable
}

configureCommon('com.threecrickets.prudence.GeneratedTextResource')
configureCommon('com.threecrickets.prudence.DelegatedResource')
configureCommon('com.threecrickets.prudence.DelegatedHandler')
configureCommon('com.threecrickets.prudence.ApplicationTask')

//
// ApplicationService
//

applicationService = new ApplicationService(applicationInstance)
