<?php
//
// Stickstick Settings
//

global $application_base, $application_base_path, $application_name, $application_description, $application_author, $application_owner, $application_home_url, $application_contact_email;
global $predefined_globals, $show_debug_on_error, $preheat_resources;

$document->execute('/defaults/application/settings/');

$application_name = 'Stickstick';
$application_description = 'Share online sticky notes';
$application_author = 'Tal Liron';
$application_owner = 'Three Crickets';
$application_home_url = 'http://threecrickets.com/prudence/stickstick/';
$application_contact_email = 'prudence@threecrickets.com';

$predefined_globals['stickstick.backend'] = 'h2';
$predefined_globals['stickstick.username'] = 'root';
$predefined_globals['stickstick.password'] = 'root';
$predefined_globals['stickstick.host'] = '';
$predefined_globals['stickstick.database'] = $application_base_path . '/data/stickstick';

$show_debug_on_error = true;

$preheat_resources = array('/data/');
?>