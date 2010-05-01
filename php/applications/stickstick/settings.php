<?php
//
// Stickstick Settings
//

global $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $runtime_attributes, $show_debug_on_error, $preheat_resources;

$executable->container->include('defaults/application/settings/');

$application_name = 'Stickstick';
$application_description = 'Share online sticky notes';
$application_author = 'Tal Liron';
$application_owner = 'Three Crickets';
$application_home_url = 'http://threecrickets.com/prudence/stickstick/';
$application_contact_email = 'prudence@threecrickets.com';

$runtime_attributes['stickstick.backend'] = 'h2';
$runtime_attributes['stickstick.username'] = 'root';
$runtime_attributes['stickstick.password'] = 'root';
$runtime_attributes['stickstick.host'] = '';
$runtime_attributes['stickstick.database'] = 'data/h2/stickstick';

$show_debug_on_error = true;

$preheat_resources = array('data/');
?>