<?php
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

global $component;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $show_debug_on_error, $show_source_code_url;
global $application_logger_name;
global $hosts, $documents_default_name, $minimum_time_between_validity_checks, $source_viewable, $libraries_base_path;
global $resources_base_url, $resources_base_path, $resources_pass_through, $resources_defrost;
global $dynamic_web_base_url, $dynamic_web_base_path, $fragments_base_path, $dynamic_web_default_document, $dynamic_web_pass_through, $dynamic_web_defrost, $dynamic_web_preheat, $dynamic_web_client_caching_mode;
global $static_web_base_url, $static_web_base_path, $static_web_compress, $static_web_directory_listing_allowed;
global $file_upload_base_path, $file_upload_size_threshold;
global $handlers_base_path;
global $tasks_base_path;
global $preheat_resources;
global $url_add_trailing_slash;
global $predefined_globals;

//
// Information
//
// These are for administrative purposes only.
//

//$application_name = 'Prudence Application'; // Defaults to the application directory name
$application_description = 'This is a Prudence application.';
$application_author = 'Anonymous';
$application_owner = 'Public Domain';
$application_home_url = 'http://threecrickets.com/prudence/';
$application_contact_email = 'prudence@threecrickets.com';

//
// Debugging
//

// Set to TRUE to show debug information on error.

$show_debug_on_error = FALSE;

// The base URL for showing source code (only relevant when showDebugOnError is TRUE). 

$show_source_code_url = '/sourcecode/';

// This is so we can see the source code for scripts by adding ?source=true
// to the URL. You probably wouldn't want this for most applications.

$source_viewable = TRUE;

//
// Performance
//

// This is the time (in milliseconds) allowed to pass until a script file
// is tested to see if it was changed. During development, you'd want this
// to be low, but during production, it should be high in order to avoid
// unnecessary hits on the filesystem.

$minimum_time_between_validity_checks = 1000;

//
// Documents
//

// If a document name points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs without relying
// on filenames.

$documents_default_name = 'default';

// Documents will always be looked for here.

$libraries_base_path = '/libraries/';

//
// Logging
//
// Logger defaults to the application's directory name. Configure logging at
// conf/logging.conf.
//

//$application_logger_name = 'prudence-application';

//
// Hosts
//
// This is a vector of vectors of two elements: the first is the virtual hosts to which,
// our application will be attached, the second is the base URLs on the hosts. See
// component/hosts.py for more information. Specify None for the URL to default to the
// application's directory name.
//

$hosts = array(array($component->defaultHost, null));

//
// Resources
//
// Sets up a directory under which you can place script files that implement
// RESTful resources. The directory structure underneath the base directory
// is directly linked to the base URL.
//

$resources_base_url = '/';
$resources_base_path = '/resources/';

// These documents are allowed to be under $libraries_base_path as well as under
// $resources_base_path.

$resources_pass_through = array();

// Set this to TRUE if you want to start to load and compile your
// resources as soon as Prudence starts.

$resources_defrost = TRUE;

//
// Dynamic Web
//
// Sets up a directory under which you can place text files that support embedded scriptlets.
// Note that the generated result can be cached for better performance.
//

$dynamic_web_base_url = '/';
$dynamic_web_base_path = '/web/dynamic/';
$fragments_base_path = '/web/fragments/';

// If the URL points to a directory rather than a file, and that directory
// contains a file with this name, then it will be used. This allows
// you to use the directory structure to create nice URLs that do not
// contain filenames.

$dynamic_web_default_document = 'index';

// These documents are allowed to be under $fragments_base_path as well as under
// $dynamic_web_base_path.

$dynamic_web_pass_through = array();

// Set this to TRUE if you want to compile your scriptlets as soon as Prudence
// starts.

$dynamic_web_defrost = TRUE;

// Set this to TRUE if you want to load all your dynamic web documents as soon
// as Prudence starts.

$dynamic_web_preheat = TRUE;

// Client caching mode: 0=disabled, 1=conditional, 2=offline

$dynamic_web_client_caching_mode = 1;

//
// Static Web
//
// Sets up a directory under which you can place static files of any type.
// Servers like Grizzly and Jetty can use non-blocking I/O to stream static
// files efficiently to clients. 
//

$static_web_base_url = '/';
$static_web_base_path = '/web/static/';

// Whether to enable smart compression on HTTP representations.

$static_web_compress = TRUE;

// If the URL points to a directory rather than a file, then this will allow
// automatic creation of an HTML page with a directory listing.

$static_web_directory_listing_allowed = TRUE;

//
// File Uploads
//

// Temporary files for uploads will be stored in this subdirectory.

$file_upload_base_path = '/uploads/';

// The size in bytes beyond which uploaded files will be stored to disk.
// Defaults to zero, meaning that all uploaded files will be stored to disk.

$file_upload_size_threshold = 0;

//
// Handlers
//
// Sets up a directory under which you can place script files that implement
// general-purpose handlers.
//

$handlers_base_path = '/handlers/';

//
// Tasks
//
// Sets up a directory where you can place script files schedule to run
// according to the application's crontab file.
//

$tasks_base_path = '/tasks/';

//
// Preheater
//
// List resources here that you want heated up as soon as Prudence starts.
//

$preheat_resources = array();

//
// URL Manipulation
//

// The URLs in this array will automatically be redirected to have a trailing
// slash added to them if it's missing.

$url_add_trailing_slash = array($dynamic_web_base_url, $static_web_base_url);

//
// Predefined Globals
//
// These will be available to your code via $application->globals.
//

$predefined_globals = array();
?>