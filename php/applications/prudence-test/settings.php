<?php
//
// Prudence Guide Settings
//

global $component, $hosts, $mysite_host;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $show_debug_on_error, $preheat_resources;

$document->execute('defaults/application/settings/');

$application_name = 'Prudence Test';
$application_description = 'Prudence tests';
$application_author = 'Tal Liron';
$application_owner = 'Three Crickets';
$application_home_url = 'http://threecrickets.com/prudence/';
$application_contact_email = 'prudence@threecrickets.com';

$hosts = array(array($component->defaultHost, NULL), array($mysite_host, NULL));

$show_debug_on_error = true;

$preheat_resources = array('/data/jython/', '/data/jruby/', '/data/groovy/', '/data/clojure/', '/data/quercus/', '/data/rhino/');
?>