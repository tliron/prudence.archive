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

application.name = application_name
application.description = application_description
application.author = application_author
application.owner = application_owner

#
# StatusService
#

application.statusService = DelegatedStatusService(show_source_code_url if show_debug_on_error else None)
application.statusService.debugging = show_debug_on_error
application.statusService.homeRef = Reference(application_home_url)
application.statusService.contactEmail = application_contact_email

#
# MetaData
#

application.metadataService.addExtension('php', MediaType.TEXT_HTML)

#
# Routing
#

execute_or_default(application_base_path + '/routing/', 'defaults/application/routing/')

#
# Logging
#

application.context.setLogger(application_logger_name)

#
# Additional/Override Runtime Attributes
#

attributes.putAll(runtime_attributes)
