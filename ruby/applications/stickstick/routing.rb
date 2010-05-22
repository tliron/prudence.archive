#
# Stickstick Routing
#

$document.execute 'defaults/application/routing/'

import com.threecrickets.prudence.util.CssUnifyMinifyFilter
import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter

$router.capture fix_url($resources_base_url + '/data/note/{id}/'), '/data/note/'

# Wrap the static web with unify-minify filters
$router.detach $static_web
$wrapped_static_web = CssUnifyMinifyFilter.new($application_instance.context, $static_web, java.io.File.new($application_base_path + $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks)
$wrapped_static_web = JavaScriptUnifyMinifyFilter.new($application_instance.context, $wrapped_static_web, java.io.File.new($application_base_path + $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks)
$router.attach(fix_url($static_web_base_url), $wrapped_static_web).matching_mode = Template::MODE_STARTS_WITH
