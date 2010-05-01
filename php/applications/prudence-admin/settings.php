<?php
//
// Prudence Admin Settings
//

global $component, $hosts, $defaultHost, $mysiteHost;
global $applicationName, $applicationDescription, $applicationAuthor, $applicationHomeURL, $applicationContactEmail;

$executable->container->include('defaults/application/settings/');

$applicationName = 'Prudence Admin';
$applicationDescription = 'Runtime management of Prudence';
$applicationAuthor = 'Tal Liron';
$applicationOwner = 'Three Crickets';
$applicationHomeURL = 'http://threecrickets.com/prudence/';
$applicationContactEmail = 'prudence@threecrickets.com';

$hosts = array(array($component->defaultHost, '/'), array($mysiteHost, '/'));
?>