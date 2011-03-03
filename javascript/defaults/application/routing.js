//
// Prudence Application Routing
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
	java.lang.ClassLoader,
	java.io.File,
	java.util.ArrayList,
	java.util.concurrent.ConcurrentHashMap,
	org.restlet.routing.Router,
	org.restlet.routing.Redirector,
	org.restlet.routing.Template,
	org.restlet.resource.Finder,
	org.restlet.resource.Directory,
	org.restlet.engine.application.Encoder,
	com.threecrickets.scripturian.util.DefrostTask,
	com.threecrickets.scripturian.document.DocumentFileSource,
	com.threecrickets.prudence.PrudenceRouter,
	com.threecrickets.prudence.util.PreheatTask,
	com.threecrickets.prudence.util.PhpExecutionController)

var classLoader = ClassLoader.systemClassLoader

//
// Utilities
//

// Makes sure we have slashes where we expect them
function fixURL(url) {
	url = url.replace(/\/\//g, '/') // no doubles
	if(url == '' || url[0] != '/') { // always at the beginning
		url = '/' + url
	}
	if(url[url.length - 1] != '/') { // always at the end
		url += '/'
	}
	return url
}

//
// Internal router
//

component.internalRouter.attach('/' + applicationInternalName + '/', applicationInstance).matchingMode = Template.MODE_STARTS_WITH

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See defaults/instance/hosts.js for more information.
//

var addTrailingSlash = new Redirector(applicationInstance.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT)

print(applicationInstance.name + ': ')
for(var i in hosts) {
	var entry = hosts[i]
	var host = entry[0]
	var url = entry[1]
	if(!url) {
		url = applicationDefaultURL
	}
	print('"' + url + '" on ' + host.name)
	if (url == '/') {
		url = ''
	}
	host.attach(url, applicationInstance).matchingMode = Template.MODE_STARTS_WITH
	if(url != '') {
		if(url[url.length - 1] == '/') {
			url = url.slice(0, -1)
		}
		host.attach(url, addTrailingSlash).matchingMode = Template.MODE_EQUALS
	}
	if(i < hosts.length - 1) {
		print(', ')
	}
}
print('.\n')

var applicationGlobals = applicationInstance.context.attributes

applicationGlobals.put('com.threecrickets.prudence.component', component)
var cache =  component.context.attributes.get('com.threecrickets.prudence.cache')
if(cache) {
	applicationGlobals.put('com.threecrickets.prudence.cache', cache)
}

//
// Inbound root
//

var router = new PrudenceRouter(applicationInstance.context, minimumTimeBetweenValidityChecks)
router.routingMode = Router.MODE_BEST_MATCH
applicationInstance.inboundRoot = router

//
// Add trailing slashes
//

for(var i in urlAddTrailingSlash) {
	urlAddTrailingSlash[i] = fixURL(urlAddTrailingSlash[i])
	if(urlAddTrailingSlash[i].length > 0) {
		if(urlAddTrailingSlash[i][urlAddTrailingSlash[i].length - 1] == '/') {
			// Remove trailing slash for pattern
			urlAddTrailingSlash[i] = urlAddTrailingSlash[i].slice(0, -1)
		}
		router.attach(urlAddTrailingSlash[i], addTrailingSlash)
	}
}

var languageManager = executable.manager

//
// Libraries
//

var libraryDocumentSources = new ArrayList(2)
libraryDocumentSources.add(new DocumentFileSource(applicationBase + librariesBasePath, applicationBasePath + librariesBasePath, documentsDefaultName, 'js', minimumTimeBetweenValidityChecks))
libraryDocumentSources.add(new DocumentFileSource(applicationBase + '/../../libraries/javascript/', applicationBasePath + '/../../libraries/javascript/', documentsDefaultName, 'js', minimumTimeBetweenValidityChecks))

//
// Dynamic web
//

var dynamicWebDocumentSource = new DocumentFileSource(applicationBase + dynamicWebBasePath, applicationBasePath + dynamicWebBasePath, dynamicWebDefaultDocument, 'js', minimumTimeBetweenValidityChecks)
var cacheKeyPatternHandlers = new ConcurrentHashMap()
applicationGlobals.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',dynamicWebDocumentSource)
applicationGlobals.put('com.threecrickets.prudence.GeneratedTextResource.defaultIncludedName', dynamicWebDefaultDocument)
applicationGlobals.put('com.threecrickets.prudence.GeneratedTextResource.executionController', new PhpExecutionController()) // Adds PHP predefined variables
applicationGlobals.put('com.threecrickets.prudence.GeneratedTextResource.clientCachingMode', dynamicWebClientCachingMode)
applicationGlobals.put('com.threecrickets.prudence.GeneratedTextResource.cacheKeyPatternHandlers', cacheKeyPatternHandlers)

var dynamicWeb = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource'))
dynamicWebBaseURL = fixURL(dynamicWebBaseURL)
router.attachBase(dynamicWebBaseURL, dynamicWeb)

if(dynamicWebDefrost) {
	var defrostTasks = DefrostTask.forDocumentSource(dynamicWebDocumentSource, languageManager, 'javascript', true, true)
	for(var i in defrostTasks) {
		tasks.push(defrostTasks[i])
	}
}

//
// Static web
//

var staticWeb = new Directory(applicationInstance.context, new File(applicationBasePath + staticWebBasePath).toURI().toString())
staticWeb.listingAllowed = staticWebDirectoryListingAllowed
staticWeb.negotiatingContent = true
staticWebBaseURL = fixURL(staticWebBaseURL)
if(staticWebCompress) {
	var encoder = new Encoder(applicationInstance.context)
	encoder.next = staticWeb
	staticWeb = encoder
}
router.attachBase(staticWebBaseURL, staticWeb)

//
// Resources
//

var resourcesDocumentSource = new DocumentFileSource(applicationBase + resourcesBasePath, applicationBasePath + resourcesBasePath, documentsDefaultName, 'js', minimumTimeBetweenValidityChecks)
applicationGlobals.put('com.threecrickets.prudence.DelegatedResource.documentSource', resourcesDocumentSource)

resources = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource'))
resourcesBaseURL = fixURL(resourcesBaseURL)
router.attachBase(resourcesBaseURL, resources)

if(resourcesDefrost) {
	var defrostTasks = DefrostTask.forDocumentSource(resourcesDocumentSource, languageManager, 'javascript', false, true)
	for(var i in defrostTasks) {
		tasks.push(defrostTasks[i])
	}
}

//
// SourceCode
//

if(showDebugOnError) {
	var documentSources = new ArrayList(2)
	documentSources.add(dynamicWebDocumentSource)
	documentSources.add(resourcesDocumentSource)
	applicationGlobals.put('com.threecrickets.prudence.SourceCodeResource.documentSources', documentSources)
	var sourceCode = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.SourceCodeResource'))
	showSourceCodeURL = fixURL(showSourceCodeURL)
	router.attach(showSourceCodeURL, sourceCode).matchingMode = Template.MODE_EQUALS
}

//
// Preheat
//

if(dynamicWebPreheat) {
	var preheatTasks = PreheatTask.forDocumentSource(dynamicWebDocumentSource, applicationInternalName, applicationInstance, applicationLoggerName)
	for(var i in preheatTasks) {
		tasks.push(preheatTasks[i])
	}
}

for(var i in preheatResources) {
	var preheatResource = preheatResources[i]
	tasks.push(new PreheatTask(applicationInternalName, preheatResource, applicationInstance, applicationLoggerName))
}
