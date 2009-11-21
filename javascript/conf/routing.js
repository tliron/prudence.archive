
// Directory
directory = new Directory(router.context, File(staticWebBasePath).toURI().toString());
directory.listingAllowed = staticWebDirectoryListingAllowed;
directory.negotiateContent = true;

// Redirect to trailing slashes
for(var i in urlAddTrailingSlash) {
	if(urlAddTrailingSlash[i].slice(-1) == '/') {
		urlAddTrailingSlash[i] = urlAddTrailingSlash[i].slice(0, -1);
	}
	var redirector = new Redirector(router.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER);
	router.attach(urlAddTrailingSlash[i], redirector).matchingMode = Template.MODE_EQUALS;
}

// Note that order of attachment is important -- first matching pattern wins
classLoader = ClassLoader.systemClassLoader;
router.attach(staticWebBaseURL, directory).matchingMode = Template.MODE_STARTS_WITH;
router.attach(resourceBaseURL, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH;
router.attach(dynamicWebBaseURL, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH;
