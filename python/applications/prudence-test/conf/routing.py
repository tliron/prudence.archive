
# Directory
directory = Directory(application.context, File(staticWebBasePath).toURI().toString())
directory.listingAllowed = staticWebDirectoryListingAllowed
directory.negotiateContent = True

# Redirect to trailing slashes
for url in urlAddTrailingSlash:
	if url[-1] == '/':
		url = url[0:-1]
	redirector = Redirector(application.context, '{ri}/', Redirector.MODE_CLIENT_SEE_OTHER)
	router.attach(url, redirector).matchingMode = Template.MODE_EQUALS

# Note that order of attachment is important -- first matching pattern wins
classLoader = ClassLoader.getSystemClassLoader()
router.attach(staticWebBaseURL, directory).matchingMode = Template.MODE_STARTS_WITH
router.attach(resourceBaseURL, classLoader.loadClass('com.threecrickets.prudence.DelegatedResource')).matchingMode = Template.MODE_STARTS_WITH
router.attach(dynamicWebBaseURL, classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource')).matchingMode = Template.MODE_STARTS_WITH
