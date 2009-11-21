//
// Component
//

// Logger used for web requests 

var componentWebLoggerName = 'web-requests';

//
// Server
//

// The TCP port on which the server will start. Note that the server can
// be set up to run behind another web server via a proxy. For Apache, this
// requires mod_proxy.

var serverPort = 8080;

//
// Application
//

var applicationName = 'Prudence Demo';
var applicationDescription = 'Used to test that Prudence works for you, and useful as a basis for creating your own applications';
var applicationAuthor = 'Tal Liron';
var applicationOwner = 'Three Crickets';
var applicationHomeURL = 'http://www.threecrickets.com/prudence/';
var applicationContactEmail = 'prudence@threecrickets.com';
var applicationLoggerName = 'prudence-demo';

// All other URLs will be under this one.

var applicationBaseURL = '/';

//
// Configuration specific to Prudence demo application
//

// Our pages will prefix this to included files. This is useful if you want to combine
// the demo site into another site.

document.meta.put('prudenceDemoBasePath', '');

//
// Resources
//
// Sets up a directory under which you can place script files that implement
// RESTful resources. The directory structure underneath the base directory
// is directly linked to the base URL.
//

var resourceBaseURL = applicationBaseURL + 'resource/';
var resourceBasePath = 'resources';

// Files with this extension can have the extension omitted from the URL,
// allowing for nicer URLs. 

var resourceExtension = 'js';

// If the URL points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs without relying
// on filenames.

var resourceDefaultName = 'default';

// This is so we can see the source code for scripts by adding ?source=true
// to the URL. You probably wouldn't want this for most applications.

var resourceSourceViewable = true;

// This is the time (in milliseconds) allowed to pass until a script file
// is tested to see if it was changed. During development, you'd want this
// to be low, but during production, it should be high in order to avoid
// unnecessary hits on the filesystem.

var resourceMinimumTimeBetweenValidityChecks = 1000;

//
// Dynamic Web
//
// Sets up a directory under which you can place text files that support embedded scriptlets.
// Note that the generated result can be cached for better performance.
//

var dynamicWebBaseURL = applicationBaseURL;
var dynamicWebBasePath = 'web';

// If the URL points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs that do not
// contain filenames.

var dynamicWebDefaultDocument = 'index.page';

// This is so we can see the source code for scripts by adding ?source=true
// to the URL. You probably wouldn't want this for most applications.

var dynamicWebSourceViewable = true;

// This is the time (in milliseconds) allowed to pass until a script file
// is tested to see if it was changed. During development, you'd want this
// to be low, but during production, it should be high in order to avoid
// unnecessary hits on the filesystem.

var dynamicWebMinimumTimeBetweenValidityChecks = 1000;

//
// Static Web
//
// Sets up a directory under which you can place static files of any type.
// Servers like Grizzly and Jetty can use non-blocking I/O to stream static
// files efficiently to clients. 
//

var staticWebBaseURL = applicationBaseURL + 'static/';
var staticWebBasePath = 'web/static';

// If the URL points to a directory rather than a file, then this will allow
// automatic creation of an HTML page with a directory listing.

var staticWebDirectoryListingAllowed = true;

//
// URL manipulation
//

// The URLs in this array will automatically be redirected to have a trailing
// slash added to them if its missing.

var urlAddTrailingSlash = [staticWebBaseURL];
