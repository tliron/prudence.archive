//
// Prudence Application Routing
//
// Note that order of attachment is important: first matching pattern wins.
//

importClass(
	java.lang.ClassLoader,
	java.io.File,
	org.restlet.routing.Router,
	org.restlet.routing.Redirector,
	org.restlet.routing.Template,
	org.restlet.resource.Finder,
	org.restlet.resource.Directory,
	com.threecrickets.prudence.internal.Fallback);

var classLoader = ClassLoader.systemClassLoader;

//
// Utilities
//

//  Makes sure we have slashes where we expect them
function fixURL(url) {
	if(url.length > 0 && url[0] == '/') {
		url = url.slice(1);
	}
	if(url.length > 0 && url[url.length -1] != '/') {
		url = url + '/';
	}
	return url;
}

// Moves a route to be the one before the last
function penultimateRoute(route) {
	router.routes.remove(route);
	router.routes.add(router.routes.size() - 1, route);
}

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See component/hosts.js for more information.
//

var redirector = new Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER);

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
		host.attach(url, redirector);
	}
	if(i < hosts.length - 1) {
		print(', ');
	}
}
print('.\n');

//
// Inbound root
//

var router = new Router(application.context);
router.routingMode = Router.MODE_BEST_MATCH;
application.inboundRoot = router;

//
// Add trailing slashes
//

if(urlAddTrailingSlash.length > 0) {
	for(var i in urlAddTrailingSlash) {
		urlAddTrailingSlash[i] = fixURL(urlAddTrailingSlash[i]);
		if(urlAddTrailingSlash[i].length > 0) {
			if(urlAddTrailingSlash[i][-1] == '/') {
				// Remove trailing slash for pattern
				urlAddTrailingSlash[i] = urlAddTrailingSlash[i].slice(0, -1);
			}
			router.attach(urlAddTrailingSlash[i], redirector);
		}
	}
}

//
// Dynamic web
//

//router.attach(fixURL(dynamicWebBaseURL), classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH;
var dynamicWeb = new Finder(application.context, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')); 

//
// Static web
//

var staticWeb = new Directory(router.context, File(applicationBasePath + staticWebBasePath).toURI().toString());
staticWeb.listingAllowed = staticWebDirectoryListingAllowed;
staticWeb.negotiateContent = true;

//router.attach(fixURL(staticWebBaseURL), staticWeb).matchingMode = Template.MODE_STARTS_WITH;

//
// Resources
//

var resources = new Finder(application.context, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource'));
//router.attach(fixURL(resourceBaseURL), classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH;

var fallback = new Fallback(application.context, dynamicWeb, staticWeb, resources);
router.attach(fixURL(dynamicWebBaseURL), fallback).matchingMode = Template.MODE_STARTS_WITH;
