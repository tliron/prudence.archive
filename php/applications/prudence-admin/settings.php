<?php
//
// Prudence Admin Settings
//

global $component, $hosts, $mysite_host;
global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;

$executable->container->execute('defaults/application/settings/');

$application_name = 'Prudence Admin';
$application_description = 'Runtime management of Prudence';
$application_author = 'Tal Liron';
$application_owner = 'Three Crickets';
$application_home_url = 'http://threecrickets.com/prudence/';
$application_contact_email = 'prudence@threecrickets.com';

$hosts = array(array($component->defaultHost, '/'), array($mysite_host, '/'));

$show_debug_on_error = TRUE;
?>