//
// Prudence Application Routing
//

importClass(
	java.lang.ClassLoader,
	java.io.File,
	javax.script.ScriptEngineManager,
	org.restlet.routing.Router,
	org.restlet.routing.Redirector,
	org.restlet.routing.Template,
	org.restlet.resource.Finder,
	org.restlet.resource.Directory,
	com.threecrickets.scripturian.DefrostTask,
	com.threecrickets.scripturian.file.DocumentFileSource,
	com.threecrickets.prudence.util.PrudenceRouter,
	com.threecrickets.prudence.util.PreheatTask);

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

component.internalRouter.attach('/' + applicationInternalName + '/', application).matchingMode = Template.MODE_STARTS_WITH;

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See component/hosts.js for more information.
//

var addTrailingSlash = new Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT);

print(application.name + ': ');
for(var i in hosts) {
	var entry = hosts[i];
	var host = entry[0];
	var url = entry[1];
	if(!url) {
		url = applicationDefaultURL;
	}
	print('"' + url + '" on ' + host.name);
	host.attach(url, application).matchingMode = Template.MODE_STARTS_WITH;
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

var attributes = application.context.attributes;

//
// Inbound root
//

var router = new PrudenceRouter(application.context);
router.routingMode = Router.MODE_BEST_MATCH;
application.inboundRoot = router;

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

var scriptEngineManager = new ScriptEngineManager();
var dynamicWebDocumentSource = new DocumentFileSource(applicationBasePath + dynamicWebBasePath, dynamicWebDefaultDocument, dynamicWebMinimumTimeBetweenValidityChecks);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.engineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource',dynamicWebDocumentSource);
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable);

var dynamicWeb = new Finder(application.context, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource'));
router.attachBase(fixURL(dynamicWebBaseURL), dynamicWeb);

if(dynamicWebDefrost) {
	var defrostTasks = DefrostTask.forDocumentSource(dynamicWebDocumentSource, scriptEngineManager, true);
	for(var i in defrostTasks) {
		tasks.push(defrostTasks[i]);
	}
}

//
// Static web
//

var staticWeb = new Directory(router.context, new File(applicationBasePath + staticWebBasePath).toURI().toString());
staticWeb.listingAllowed = staticWebDirectoryListingAllowed;
staticWeb.negotiatingContent = true;
router.attachBase(fixURL(staticWebBaseURL), staticWeb);

//
// Resources
//

var resourcesDocumentSource = new DocumentFileSource(applicationBasePath + resourcesBasePath, resourcesDefaultName, resourcesMinimumTimeBetweenValidityChecks);
attributes.put('com.threecrickets.prudence.DelegatedResource.engineManager', scriptEngineManager);
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultEngineName', 'rhino-nonjdk');
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourcesDefaultName);
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource', resourcesDocumentSource);
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourcesSourceViewable);

resources = new Finder(application.context, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource'));
router.attachBase(fixURL(resourcesBaseURL), resources);

if(resourcesDefrost) {
	var defrostTasks = DefrostTask.forDocumentSource(resourcesDocumentSource, scriptEngineManager, true);
	for(var i in defrostTasks) {
		tasks.push(defrostTasks[i]);
	}
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
