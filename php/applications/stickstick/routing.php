<?php
//
// Stickstick Routing
//

global $router, $resources_base_url, $static_web, $static_web_base_url, $application_instance, $application_base_path;

import com.threecrickets.prudence.util.CssUnifyMinifyFilter;
import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter;

$document->execute('defaults/application/routing/');

$router->capture($resources_base_url . 'data/note/{id}/', 'data/note/');
$router->hide('data/note/');

// Wrap the static web with unify-minify filters
$router->detach($static_web);
$wrapped_static_web = new CssUnifyMinifyFilter($application_instance->context, $static_web, new File($application_base_path . $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks);
$wrapped_static_web = new JavaScriptUnifyMinifyFilter($application_instance->context, $wrapped_static_web, new File($application_base_path . $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks);
$router->attachBase($static_web_base_url, $wrapped_static_web);
?>