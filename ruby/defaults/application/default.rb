#
# Prudence Application
#

import org.restlet.data.Reference
import org.restlet.data.MediaType
import com.threecrickets.prudence.util.DelegatedStatusService

#
# Settings
#

execute_or_default $application_base_path + '/settings/', 'defaults/application/settings/'

#
# Application
#

execute_or_default $application_base_path + '/application/', 'defaults/application/application/'

$application_instance.name = $application_name
$application_instance.description = $application_description
$application_instance.author = $application_author
$application_instance.owner = $application_owner

#
# StatusService
#

$application_instance.status_service = DelegatedStatusService.new($show_debug_on_error ? $show_source_code_url : nil)
$application_instance.status_service.debugging = $show_debug_on_error
$application_instance.status_service.home_ref = Reference.new $application_home_url
$application_instance.status_service.contact_email = $application_contact_email

#
# MetaData
#

$application_instance.metadata_service.add_extension 'php', MediaType::TEXT_HTML

#
# Routing
#

execute_or_default $application_base_path + '/routing/', 'defaults/application/routing/'

#
# Logging
#

$application_instance.context.set_logger $application_logger_name

#
# Predfined Globals
#

for key in $predefined_globals.keys
	$attributes[key] = $predefined_globals[key]
end
