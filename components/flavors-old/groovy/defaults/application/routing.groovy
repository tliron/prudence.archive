//
// Prudence Application Routing
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

import java.lang.ClassLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import org.restlet.routing.Router
import org.restlet.routing.Redirector
import org.restlet.routing.Template
import org.restlet.resource.Finder
import org.restlet.resource.Directory
import org.restlet.engine.application.Encoder
import com.threecrickets.scripturian.util.DefrostTask
import com.threecrickets.scripturian.document.DocumentFileSource
import com.threecrickets.prudence.PrudenceRouter
import com.threecrickets.prudence.util.Fallback
import com.threecrickets.prudence.util.PreheatTask
import com.threecrickets.prudence.util.PhpExecutionController

classLoader = ClassLoader.systemClassLoader

//
// Utilities
//

// Makes sure we have slashes where we expect them
fixURL = { url ->
	url = (url =~ /\/\//).replaceAll('/') // no doubles
	if(url == '' || url[0] != '/') { // always at the beginning
		url = '/' + url
	}
	if((url != '/') && (url[url.size() - 1] != '/')) { // always at the end
		url += '/'
	}
	return url
}

//
// Internal router
//

componentInstance.internalRouter.attach('/' + applicationInternalName, applicationInstance).matchingMode = Template.MODE_STARTS_WITH

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See defaults/instance/hosts.groovy for more information.
//

addTrailingSlash = new Redirector(applicationInstance.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT)

print(applicationInstance.name + ': ')
hosts.eachWithIndex() { entry, i ->
	host = entry[0]
	url = entry[1]
	if(!url) {
		url = applicationDefaultURL
	}
	if((url != '') && (url[url.size() - 1] == '/')) {
		// No trailing slash
		url = url.substring(0, url.size() - 1)
	}
	print('"' + url + '/" on ' + host.name)
	host.attach(url, applicationInstance).matchingMode = Template.MODE_STARTS_WITH
	if(url != '') {
		host.attach(url, addTrailingSlash).matchingMode = Template.MODE_EQUALS
	}
	if(i < hosts.size() - 1) {
		print(', ')
	}
}
println('.')
out.flush()

applicationGlobals = applicationInstance.context.attributes

applicationGlobals['com.threecrickets.prudence.component'] = componentInstance
cache =  componentInstance.context.attributes['com.threecrickets.prudence.cache']
if(cache) {
	applicationGlobals['com.threecrickets.prudence.cache'] = cache
}

//
// Inbound root
//

router = new PrudenceRouter(applicationInstance.context, minimumTimeBetweenValidityChecks)
router.routingMode = Router.MODE_BEST_MATCH
applicationInstance.inboundRoot = router

//
// Add trailing slashes
//

for(url in urlAddTrailingSlash) {
	url = fixURL(url)
	if(url.size() > 0) {
		if(url[url.size() - 1] == '/') {
			// Remove trailing slash for pattern
			url = url.substring(0, url.size() - 1)
		}
		router.attach(url, addTrailingSlash)
	}
}

languageManager = executable.manager

//
// Libraries
//

librariesDocumentSources = new CopyOnWriteArrayList()
librariesDocumentSources.add(new DocumentFileSource(applicationBase + librariesBasePath, applicationBasePath + librariesBasePath, documentsDefaultName, 'groovy', minimumTimeBetweenValidityChecks))
librariesDocumentSources.add(commonLibrariesDocumentSource)

//
// Dynamic web
//

dynamicWebDocumentSource = new DocumentFileSource(applicationBase + dynamicWebBasePath, applicationBasePath + dynamicWebBasePath, dynamicWebDefaultDocument, 'groovy', minimumTimeBetweenValidityChecks)
fragmentsDocumentSources = new CopyOnWriteArrayList()
fragmentsDocumentSources.add(new DocumentFileSource(applicationBase + fragmentsBasePath, applicationBasePath + fragmentsBasePath, dynamicWebDefaultDocument, 'groovy', minimumTimeBetweenValidityChecks))
fragmentsDocumentSources.add(commonFragmentsDocumentSource)
cacheKeyPatternHandlers = new ConcurrentHashMap()
scriptletPlugins = new ConcurrentHashMap()
passThroughDocuments = new CopyOnWriteArraySet()
passThroughDocuments.addAll(dynamicWebPassThrough)
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = dynamicWebDocumentSource
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.extraDocumentSources'] = fragmentsDocumentSources
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.passThroughDocuments'] = passThroughDocuments
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName'] = dynamicWebDefaultDocument
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.executionController'] = new PhpExecutionController() // Adds PHP predefined variables
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.clientCachingMode'] = dynamicWebClientCachingMode
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers'] = cacheKeyPatternHandlers
applicationGlobals['com.threecrickets.prudence.GeneratedTextResource.scriptletPlugins'] = scriptletPlugins

dynamicWeb = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource'))
dynamicWebBaseURL = fixURL(dynamicWebBaseURL)
router.attachBase(dynamicWebBaseURL, dynamicWeb)

if(dynamicWebDefrost) {
	defrostTasks = DefrostTask.forDocumentSource(dynamicWebDocumentSource, languageManager, 'groovy', true, true)
	for(defrostTask in defrostTasks) {
		tasks.push(defrostTask)
	}
}

//
// Static web
//

staticWeb = new Fallback(applicationInstance.context, minimumTimeBetweenValidityChecks)
directory = new Directory(applicationInstance.context, new File(applicationBasePath + staticWebBasePath).toURI().toString())
directory.listingAllowed = staticWebDirectoryListingAllowed
directory.negotiatingContent = true
staticWeb.addTarget(directory)
directory = new Directory(applicationInstance.context, new File(document.source.basePath, 'common/web/static/').toURI().toString())
directory.listingAllowed = staticWebDirectoryListingAllowed
directory.negotiatingContent = true
staticWeb.addTarget(directory)

staticWebBaseURL = fixURL(staticWebBaseURL)
if(staticWebCompress) {
	encoder = new Encoder(applicationInstance.context)
	encoder.next = staticWeb
	staticWeb = encoder
}
router.attachBase(staticWebBaseURL, staticWeb)

//
// Resources
//

resourcesDocumentSource = new DocumentFileSource(applicationBase + resourcesBasePath, applicationBasePath + resourcesBasePath, documentsDefaultName, 'groovy', minimumTimeBetweenValidityChecks)
passThroughDocuments = new CopyOnWriteArraySet()
passThroughDocuments.addAll(resourcesPassThrough)
applicationGlobals['com.threecrickets.prudence.DelegatedResource.documentSource'] = resourcesDocumentSource
applicationGlobals['com.threecrickets.prudence.DelegatedResource.passThroughDocuments'] = passThroughDocuments

resources = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource'))
resourcesBaseURL = fixURL(resourcesBaseURL)
router.attachBase(resourcesBaseURL, resources)

if(resourcesDefrost) {
	defrostTasks = DefrostTask.forDocumentSource(resourcesDocumentSource, languageManager, 'groovy', false, true)
	for(defrostTask in defrostTasks) {
		tasks.push(defrostTask)
	}
}

//
// SourceCode
//

if(showDebugOnError) {
	applicationGlobals['com.threecrickets.prudence.SourceCodeResource.documentSources'] = [dynamicWebDocumentSource, resourcesDocumentSource]
	sourceCode = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.SourceCodeResource'))
	showSourceCodeURL = fixURL(showSourceCodeURL)
	router.attach(showSourceCodeURL, sourceCode).matchingMode = Template.MODE_EQUALS
}

//
// Preheat
//

if(dynamicWebPreheat) {
	preheatTasks = PreheatTask.forDocumentSource(dynamicWebDocumentSource, applicationInternalName, applicationInstance, applicationLoggerName)
	for(preheatTask in preheatTasks) {
		tasks.push(preheatTask)
	}
}

for(preheatResource in preheatResources) {
	tasks.push(new PreheatTask(applicationInternalName, preheatResource, applicationInstance, applicationLoggerName))
}
