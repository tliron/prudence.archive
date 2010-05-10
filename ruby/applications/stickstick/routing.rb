#
# Stickstick Routing
#

$document.execute 'defaults/application/routing/'

import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter

$router.capture fix_url($resources_base_url + '/data/note/{id}/'), '/data/note/'

# Wrap the static web with a JavaScriptUnifyMinifyFilter
$router.detach $static_web
$router.attach(fix_url($static_web_base_url), \
	JavaScriptUnifyMinifyFilter.new($application_instance.context, $static_web, java.io.File.new($application_base_path + $static_web_base_path), $dynamic_web_minimum_time_between_validity_checks)) \
	.matching_mode = Template::MODE_STARTS_WITH
