#
# Prudence Application
#

import org.restlet.data.Reference
import org.restlet.data.MediaType
import com.threecrickets.prudence.util.DelegatedStatusService

#
# Settings
#

include_or_default $application_base_path + '/settings/', 'defaults/application/settings/'

#
# Application
#

include_or_default $application_base_path + '/application/', 'defaults/application/application/'

$application.name = $application_name
$application.description = $application_description
$application.author = $application_author
$application.owner = $application_owner

#
# StatusService
#

$application.status_service = DelegatedStatusService.new($show_debug_on_error ? $show_source_code_url : nil)
$application.status_service.debugging = $show_debug_on_error
$application.status_service.home_ref = Reference.new $application_home_url
$application.status_service.contact_email = $application_contact_email

#
# MetaData
#

$application.metadata_service.add_extension 'php', MediaType::TEXT_HTML

#
# Routing
#

include_or_default $application_base_path + '/routing/', 'defaults/application/routing/'

#
# Logging
#

$application.context.set_logger $application_logger_name

#
# Additional/Override Runtime Attributes
#

for key in $runtime_attributes.keys
	$attributes[key] = $runtime_attributes[key]
end
