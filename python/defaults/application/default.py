#
# Prudence Application
#

from org.restlet.data import Reference, MediaType
from com.threecrickets.scripturian.file import DocumentFileSource
from com.threecrickets.prudence.util import DelegatedStatusService

#
# Settings
#

include_or_default(application_base_path + '/settings', 'defaults/application/settings')

#
# Application
#

include_or_default(application_base_path + '/application', 'defaults/application/application')

application.name = application_name
application.description = application_description
application.author = application_author
application.owner = application_owner

attributes = application.context.attributes

#
# StatusService
#

application.statusService = DelegatedStatusService()
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

include_or_default(application_base_path + '/routing', 'defaults/application/routing')

#
# Logging
#

application.context.setLogger(application_logger_name)

#
# Additional/Override Runtime Attributes
#

attributes.putAll(runtime_attributes)
