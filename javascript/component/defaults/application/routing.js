//document.container.include('component/defaults/application/routing');

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
	org.restlet.resource.Directory);

var classLoader = ClassLoader.systemClassLoader;

function applicationURL(url) {
	return (applicationBaseURL + url).replace('//', '/');
}

//
// Hosts
//
// Note that the application's context will not be created until we attach the application to at least one
// virtual host. See start/hosts.js for more information.
//

print('Attached application "' + application.name + '" to "' + applicationBaseURL + '" on virtual hosts ');
for(var i in hosts) {
	var host = hosts[i];
	host.attach(application);
	print('"' + host.name + '"');
	if(i < hosts.length - 1) {
		print(', ');
	}
}
print('.\n');

//
// Inbound root
//

var router = new Router(application.context);
application.inboundRoot = router;

//
// Add trailing slashes
//

if(urlAddTrailingSlash.length > 0) {
	var redirector = new Redirector(router.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER);
	for(var i in urlAddTrailingSlash) {
		urlAddTrailingSlash[i] = applicationURL(urlAddTrailingSlash[i]);
		if(urlAddTrailingSlash[i].slice(-1) == '/') {
			// Remove trailing slash for pattern
			urlAddTrailingSlash[i] = urlAddTrailingSlash[i].slice(0, -1);
		}
		if(urlAddTrailingSlash[i].length > 0) {
			router.attach(urlAddTrailingSlash[i], redirector);
		}
	}
}

//
// Static web
//

var staticWeb = new Directory(router.context, File(applicationBasePath + staticWebBasePath).toURI().toString());
staticWeb.listingAllowed = staticWebDirectoryListingAllowed;
staticWeb.negotiateContent = true;

router.attach(applicationURL(staticWebBaseURL), staticWeb).matchingMode = Template.MODE_STARTS_WITH;

//
// Resources
//

router.attach(applicationURL(resourceBaseURL), classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH;

//
// Dynamic web
//

router.attach(applicationURL(dynamicWebBaseURL), classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH;
