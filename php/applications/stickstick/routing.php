<?php
//
// Stickstick Routing
//

global $router, $resources_base_url, $static_web, $static_web_base_url, $application_instance, $application_base_path;

import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter;

$document->execute('defaults/application/routing/');

$router->capture(fix_url($resources_base_url . '/data/note/{id}/'), '/data/note/');

// Wrap the static web with a JavaScriptUnifyMinifyFilter
$router->detach($static_web);
$router->attach(fix_url($static_web_base_url),
	new JavaScriptUnifyMinifyFilter($application_instance->context, $static_web, new File($application_base_path . $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks))
	->matchingMode = Template::MODE_STARTS_WITH;
?>