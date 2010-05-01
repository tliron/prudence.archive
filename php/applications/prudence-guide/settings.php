<?php
//
// Prudence Guide Settings
//

global $component, $hosts, $defaultHost, $mysiteHost;
global $applicationName, $applicationDescription, $applicationAuthor, $applicationHomeURL, $applicationContactEmail;
global $showDebugOnError, $preheatResources;

$executable->container->include('defaults/application/settings/');

$applicationName = 'Prudence Guide';
$applicationDescription = 'Prudence web site, documentation, and tests';
$applicationAuthor = 'Tal Liron';
$applicationOwner = 'Three Crickets';
$applicationHomeURL = 'http://threecrickets.com/prudence/';
$applicationContactEmail = 'prudence@threecrickets.com';

$hosts = array(array($component->defaultHost, NULL), array($mysiteHost, NULL));

$showDebugOnError = true;

$preheatResources = array('/data/jython/', '/data/jruby/', '/data/groovy/', '/data/clojure/', '/data/rhino/');
?>