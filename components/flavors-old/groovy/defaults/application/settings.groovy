//
// Prudence Application Settings
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

//
// Information
//
// These are for administrative purposes only.
//

//applicationName = 'Prudence Application' // Defaults to the application directory name
applicationDescription = 'This is a Prudence application.'
applicationAuthor = 'Anonymous'
applicationOwner = 'Public Domain'
applicationHomeURL = 'http://threecrickets.com/prudence/'
applicationContactEmail = 'prudence@threecrickets.com'

//
// Debugging
//

// Set to true to show debug information on error.

showDebugOnError = false

// The base URL for showing source code (only relevant when showDebugOnError is true). 

showSourceCodeURL = '/sourcecode/'

// This is so we can see the source code for scripts by adding ?source=true
// to the URL. You probably wouldn't want this for most applications.

sourceViewable = true

//
// Performance
//

// This is the time (in milliseconds) allowed to pass until a script file
// is tested to see if it was changed. During development, you'd want this
// to be low, but during production, it should be high in order to avoid
// unnecessary hits on the filesystem.

minimumTimeBetweenValidityChecks = 1000

//
// Documents
//

// If a document name points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs without relying
// on filenames.

documentsDefaultName = 'default'

// Documents will always be looked for here.

librariesBasePath = '/libraries/'

//
// Logging
//
// Logger defaults to the application's directory name. Configure logging at
// conf/logging.conf.
//

//applicationLoggerName = 'prudence-application'

//
// Hosts
//
// This is a vector of vectors of two elements: the first is the virtual hosts to which,
// our application will be attached, the second is the base URLs on the hosts. See
// componentInstance/hosts.py for more information. Specify None for the URL to default to the
// application's directory name.
//

hosts = [[componentInstance.defaultHost, null]]

//
// Resources
//
// Sets up a directory under which you can place script files that implement
// RESTful resources. The directory structure underneath the base directory
// is directly linked to the base URL.
//

resourcesBaseURL = '/'
resourcesBasePath = '/resources/'

// These documents are allowed to be under librariesBasePath as well as under
// resourcesBasePath.

resourcesPassThrough = []

// Set this to true if you want to start to load and compile your
// resources as soon as Prudence starts.

resourcesDefrost = true

//
// Dynamic Web
//
// Sets up a directory under which you can place text files that support embedded scriptlets.
// Note that the generated result can be cached for better performance.
//

dynamicWebBaseURL = '/'
dynamicWebBasePath = '/web/dynamic/'
fragmentsBasePath = '/web/fragments/'

// If the URL points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs that do not
// contain filenames.

dynamicWebDefaultDocument = 'index'

// These documents are allowed to be under fragmentsBasePath as well as under
// dynamicWebBasePath.

dynamicWebPassThrough = []

// Set this to true if you want to compile your scriptlets as soon as Prudence
// starts.

dynamicWebDefrost = true

// Set this to true if you want to load all your dynamic web documents as soon
// as Prudence starts.

dynamicWebPreheat = true

// Client caching mode: 0=disabled, 1=conditional, 2=offline

dynamicWebClientCachingMode = 1

//
// Static Web
//
// Sets up a directory under which you can place static files of any type.
// Servers like Grizzly and Jetty can use non-blocking I/O to stream static
// files efficiently to clients. 
//

staticWebBaseURL = '/'
staticWebBasePath = '/web/static/'

// Whether to enable smart compression on HTTP representations.

staticWebCompress = true

// If the URL points to a directory rather than a file, then this will allow
// automatic creation of an HTML page with a directory listing.

staticWebDirectoryListingAllowed = true

//
// File Uploads
//

// Temporary files for uploads will be stored in this subdirectory.

fileUploadBasePath = '/uploads/'

// The size in bytes beyond which uploaded files will be stored to disk.
// Defaults to zero, meaning that all uploaded files will be stored to disk.

fileUploadSizeThreshold = 0

//
// Handlers
//
// Sets up a directory under which you can place script files that implement
// general-purpose handlers.
//

handlersBasePath = '/handlers/'

//
// Tasks
//
// Sets up a directory where you can place script files schedule to run
// according to the application's crontab file.
//

tasksBasePath = '/tasks/'

//
// Preheater
//
// List resources here that you want heated up as soon as Prudence starts.
//

preheatResources = []

//
// URL Manipulation
//

// The URLs in this array will automatically be redirected to have a trailing
// slash added to them if it's missing.

urlAddTrailingSlash = [dynamicWebBaseURL, staticWebBaseURL]

//
// Predefined Globals
//
// These will be available to your code via application.globals.
//

predefinedGlobals = [:]
