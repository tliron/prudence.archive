//
// Prudence Application Settings
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

//
// Information
//
// These are for administrative purposes only.
//

//var applicationName = 'Prudence Application' // Defaults to the application directory name
var applicationDescription = 'This is a Prudence application.'
var applicationAuthor = 'Anonymous'
var applicationOwner = 'Public Domain'
var applicationHomeURL = 'http://threecrickets.com/prudence/'
var applicationContactEmail = 'prudence@threecrickets.com'

//
// Debugging
//

// Set to true to show debug information on error.

var showDebugOnError = false

// The base URL for showing source code (only relevant when showDebugOnError is true). 

var showSourceCodeURL = '/sourcecode/'

// This is so we can see the source code for scripts by adding ?source=true
// to the URL. You probably wouldn't want this for most applications.

var sourceViewable = true

//
// Performance
//

// This is the time (in milliseconds) allowed to pass until a script file
// is tested to see if it was changed. During development, you'd want this
// to be low, but during production, it should be high in order to avoid
// unnecessary hits on the filesystem.

var minimumTimeBetweenValidityChecks = 1000

//
// Documents
//

// If a document name points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs without relying
// on filenames.

var documentsDefaultName = 'default'

// Documents will always be looked for here.

var librariesBasePath = '/libraries/'

//
// Logging
//
// Logger defaults to the application's directory name. Configure logging at
// conf/logging.conf.
//

//var applicationLoggerName = 'prudence-application'

//
// Hosts
//
// This is a vector of vectors of two elements: the first is the virtual hosts to which,
// our application will be attached, the second is the base URLs on the hosts. See
// component/hosts.py for more information. Specify None for the URL to default to the
// application's directory name.
//

var hosts = [[component.defaultHost, null]]

//
// Resources
//
// Sets up a directory under which you can place script files that implement
// RESTful resources. The directory structure underneath the base directory
// is directly linked to the base URL.
//

var resourcesBaseURL = '/'
var resourcesBasePath = '/resources/'

// Set this to true if you want to start to load and compile your
// resources as soon as Prudence starts.

var resourcesDefrost = true

//
// Dynamic Web
//
// Sets up a directory under which you can place text files that support embedded scriptlets.
// Note that the generated result can be cached for better performance.
//

var dynamicWebBaseURL = '/'
var dynamicWebBasePath = '/web/dynamic/'

// If the URL points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs that do not
// contain filenames.

var dynamicWebDefaultDocument = 'index'

// Set this to true if you want to compile your scriptlets as soon as Prudence
// starts.

var dynamicWebDefrost = true

// Set this to true if you want to load all your dynamic web documents as soon
// as Prudence starts.

var dynamicWebPreheat = true

// Client caching mode: 0=disabled, 1=conditional, 2=offline

var dynamicWebClientCachingMode = 1

//
// Static Web
//
// Sets up a directory under which you can place static files of any type.
// Servers like Grizzly and Jetty can use non-blocking I/O to stream static
// files efficiently to clients. 
//

var staticWebBaseURL = '/'
var staticWebBasePath = '/web/static/'

// Whether to enable smart compression on HTTP representations.

var staticWebCompress = true

// If the URL points to a directory rather than a file, then this will allow
// automatic creation of an HTML page with a directory listing.

var staticWebDirectoryListingAllowed = true

//
// File Uploads
//

// The size in bytes beyond which uploaded files will be stored to disk.
// Defaults to zero, meaning that all uploaded files will be stored to disk.

var fileUploadSizeThreshold = 0

//
// Handlers
//
// Sets up a directory under which you can place script files that implement
// general-purpose handlers.
//

var handlersBasePath = '/handlers/'

//
// Tasks
//
// Sets up a directory where you can place script files schedule to run
// according to the application's crontab file.
//

var tasksBasePath = '/tasks/'

//
// Preheater
//
// List resources here that you want heated up as soon as Prudence starts.
//

var preheatResources = []

//
// URL Manipulation
//

// The URLs in this array will automatically be redirected to have a trailing
// slash added to them if it's missing.

var urlAddTrailingSlash = [dynamicWebBaseURL, staticWebBaseURL]

//
// Predefined Globals
//
// These will be available to your code via application.globals.
//

predefinedGlobals = {}
