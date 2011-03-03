<?php
//
// Stickstick Routing
//

global $router, $resources_base_url, $static_web, $static_web_base_url, $application_instance, $application_base_path;

import com.threecrickets.prudence.util.CssUnifyMinifyFilter;
import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter;

$document->execute('/defaults/application/routing/');

$router->captureAndHide($resources_base_url . 'data/note/{id}/', '/data/note/');

// Wrap the static web with unify-minify filters
$css_filter = new CssUnifyMinifyFilter(NULL, new File($application_base_path . $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks);
$java_script_filter = new JavaScriptUnifyMinifyFilter(NULL, new File($application_base_path . $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks);
$router->filterBase($static_web_base_url, $css_filter, $static_web);
$router->filterBase($static_web_base_url, $java_script_filter, $css_filter);
?>