
document.execute('defaults/application/routing/')

from com.threecrickets.prudence.util import JavaScriptUnifyMinifyFilter

router.capture(fix_url(resources_base_url + '/data/note/{id}/'), '/data/note/')

# Wrap the static web with a JavaScriptUnifyMinifyFilter
router.detach(static_web)
router.attach(fix_url(static_web_base_url),
    JavaScriptUnifyMinifyFilter(application_instance.context, static_web, File(application_base_path + static_web_base_path), dynamic_web_minimum_time_between_validity_checks)).matchingMode = Template.MODE_STARTS_WITH
