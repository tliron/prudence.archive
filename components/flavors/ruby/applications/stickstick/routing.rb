#
# Stickstick Routing
#

$document.execute '/defaults/application/routing/'

import com.threecrickets.prudence.util.CssUnifyMinifyFilter
import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter

$router.capture_and_hide $resources_base_url + 'data/note/{id}/', '/data/note/'

# Wrap the static web with unify-minify filters
$css_filter = CssUnifyMinifyFilter.new(nil, java.io.File.new($application_base_path + $static_web_base_path), $minimum_time_between_validity_checks)
$java_script_filter = JavaScriptUnifyMinifyFilter.new(nil, java.io.File.new($application_base_path + $static_web_base_path), $minimum_time_between_validity_checks)
$router.filter_base $static_web_base_url, $css_filter, $static_web
$router.filter_base $static_web_base_url, $java_script_filter, $css_filter
