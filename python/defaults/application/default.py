#
# Prudence Application
#

from org.restlet.data import Reference, MediaType
from com.threecrickets.prudence.util import DelegatedStatusService

#
# Settings
#

execute_or_default(application_base_path + '/settings/', 'defaults/application/settings/')

#
# Application
#

execute_or_default(application_base_path + '/application/', 'defaults/application/application/')

application_instance.name = application_name
application_instance.description = application_description
application_instance.author = application_author
application_instance.owner = application_owner

#
# StatusService
#

application_instance.statusService = DelegatedStatusService(show_source_code_url if show_debug_on_error else None)
application_instance.statusService.debugging = show_debug_on_error
application_instance.statusService.homeRef = Reference(application_home_url)
application_instance.statusService.contactEmail = application_contact_email

#
# MetaData
#

application_instance.metadataService.addExtension('php', MediaType.TEXT_HTML)

#
# Routing
#

execute_or_default(application_base_path + '/routing/', 'defaults/application/routing/')

#
# Logging
#

application_instance.context.setLogger(application_logger_name)

#
# Additional/Override Runtime Attributes
#

attributes.putAll(runtime_attributes)
