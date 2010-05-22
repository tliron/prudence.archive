
document.execute('defaults/application/routing/')

from com.threecrickets.prudence.util import CssUnifyMinifyFilter, JavaScriptUnifyMinifyFilter

router.capture(fix_url(resources_base_url + '/data/note/{id}/'), '/data/note/')

# Wrap the static web with unify-minify filters
router.detach(static_web)
wrapped_static_web = CssUnifyMinifyFilter(application_instance.context, static_web, File(application_base_path + static_web_base_path), dynamic_web_minimum_time_between_validity_checks)
wrapped_static_web = JavaScriptUnifyMinifyFilter(application_instance.context, wrapped_static_web, File(application_base_path + static_web_base_path), dynamic_web_minimum_time_between_validity_checks)
router.attach(fix_url(static_web_base_url), wrapped_static_web).matchingMode = Template.MODE_STARTS_WITH
