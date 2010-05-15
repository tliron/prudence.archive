//
// Prudence Application Routing
//

importClass(
	java.lang.ClassLoader,
	java.io.File,
	java.util.ArrayList,
	org.restlet.routing.Router,
	org.restlet.routing.Redirector,
	org.restlet.routing.Template,
	org.restlet.resource.Finder,
	org.restlet.resource.Directory,
	com.threecrickets.scripturian.util.DefrostTask,
	com.threecrickets.scripturian.document.DocumentFileSource,
	com.threecrickets.prudence.util.PrudenceRouter,
	com.threecrickets.prudence.util.PreheatTask,
	com.threecrickets.prudence.util.PhpExecutionController);

var classLoader = ClassLoader.systemClassLoader;

//
// Utilities
//

// Makes sure we have slashes where we expect them
function fixURL(url) {
	url = url.replace(/\/\//g, '/'); // no doubles
	if(url.length > 0 && url[0] == '/') { // never at the beginning
		url = url.slice(1);
	}
	if(url.length > 0 && url[url.length - 1] != '/') { // always at the end
		url += '/';
	}
	return url;
}

//
// Internal router
//

component.internalRouter.attach('/' + applicationInternalName + '/', applicationInstance).matchingMode = Template.MODE_STARTS_WITH;

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See defaults/instance/hosts.js for more information.
//

var addTrailingSlash = new Redirector(applicationInstance.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT);

print(applicationInstance.name + ': ');
for(var i in hosts) {
	var entry = hosts[i];
	var host = entry[0];
	var url = entry[1];
	if(!url) {
		url = applicationDefaultURL;
	}
	print('"' + url + '" on ' + host.name);
	host.attach(url, applicationInstance).matchingMode = Template.MODE_STARTS_WITH;
	if(url != '/') {
		if(url[url.length - 1] == '/') {
			url = url.slice(0, -1);
		}
		host.attach(url, addTrailingSlash).matchingMode = Template.MODE_EQUALS;
	}
	if(i < hosts.length - 1) {
		print(', ');
	}
}
print('.\n');

var attributes = applicationInstance.context.attributes;

attributes.put('component', component);
var cache =  component.context.attributes.get('com.threecrickets.prudence.cache');
if(cache) {
	attributes.put('com.threecrickets.prudence.cache', cache);
}

//
// Inbound root
//

var router = new PrudenceRouter(applicationInstance.context);
router.routingMode = Router.MODE_BEST_MATCH;
applicationInstance.inboundRoot = router;

//
// Add trailing slashes
//

for(var i in urlAddTrailingSlash) {
	urlAddTrailingSlash[i] = fixURL(urlAddTrailingSlash[i]);
	if(urlAddTrailingSlash[i].length > 0) {
		if(urlAddTrailingSlash[i][urlAddTrailingSlash[i].length - 1] == '/') {
			// Remove trailing slash for pattern
			urlAddTrailingSlash[i] = urlAddTrailingSlash[i].slice(0, -1);
		}
		router.attach(urlAddTrailingSlash[i], addTrailingSlash);
	}
}

//
// Dynamic web
//

var languageManager = executable.manager;
var dynamicWebDocumentSource = new DocumentFileSource(applicationBasePath + dynamicWebBasePath, dynamicWebDefaultDocument, dynamicWebMinimumTimeBetweenValidityChecks);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.languageManager', languageManager);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultLanguageTag', 'javascript');
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',dynamicWebDocumentSource);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.executionController', new PhpExecutionController()); // Adds PHP predefined variables

var dynamicWeb = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource'));
router.attachBase(fixURL(dynamicWebBaseURL), dynamicWeb);

if(dynamicWebDefrost) {
	var defrostTasks = DefrostTask.forDocumentSource(dynamicWebDocumentSource, languageManager, 'javascript', true, true);
	for(var i in defrostTasks) {
		tasks.push(defrostTasks[i]);
	}
}

//
// Static web
//

var staticWeb = new Directory(applicationInstance.context, new File(applicationBasePath + staticWebBasePath).toURI().toString());
staticWeb.listingAllowed = staticWebDirectoryListingAllowed;
staticWeb.negotiatingContent = true;
router.attachBase(fixURL(staticWebBaseURL), staticWeb);

//
// Resources
//

var resourcesDocumentSource = new DocumentFileSource(applicationBasePath + resourcesBasePath, resourcesDefaultName, resourcesMinimumTimeBetweenValidityChecks);
attributes.put('com.threecrickets.prudence.DelegatedResource.languageManager', languageManager);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultLanguageTag', 'javascript');
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourcesDefaultName);
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource', resourcesDocumentSource);
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourcesSourceViewable);

resources = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource'));
router.attachBase(fixURL(resourcesBaseURL), resources);

if(resourcesDefrost) {
	var defrostTasks = DefrostTask.forDocumentSource(resourcesDocumentSource, languageManager, 'javascript', false, true);
	for(var i in defrostTasks) {
		tasks.push(defrostTasks[i]);
	}
}

//
// SourceCode
//

if(showDebugOnError) {
	var documentSources = new ArrayList();
	documentSources.add(dynamicWebDocumentSource);
	documentSources.add(resourcesDocumentSource);
	attributes.put('com.threecrickets.prudence.SourceCodeResource.documentSources', documentSources);
	var sourceCode = new Finder(applicationInstance.context, classLoader.loadClass('com.threecrickets.prudence.SourceCodeResource'));
	router.attach(fixURL(showSourceCodeURL), sourceCode).matchingMode = Template.MODE_EQUALS;
}

//
// Preheat
//

if(dynamicWebPreheat) {
	var preheatTasks = PreheatTask.forDocumentSource(dynamicWebDocumentSource, component.context, applicationInternalName);
	for(var i in preheatTasks) {
		tasks.push(preheatTasks[i]);
	}
}

for(var i in preheatResources) {
	var preheatResource = preheatResources[i];
	tasks.push(new PreheatTask(component.context, applicationInternalName, preheatResource));
}
