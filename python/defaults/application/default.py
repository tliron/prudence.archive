#
# Prudence Application
#

from java.io import FileNotFoundException
from javax.script import ScriptEngineManager

from org.restlet import Application
from org.restlet.data import Reference, MediaType, Status
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
application.statusService = DelegatedStatusService()
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
# Configuration
#

attributes = application.context.attributes

script_engine_manager = ScriptEngineManager()

# DelegatedResource

attributes['com.threecrickets.prudence.DelegatedResource.engineManager'] = script_engine_manager
attributes['com.threecrickets.prudence.DelegatedResource.defaultEngineName'] = 'python'
attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = resource_default_name
attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = \
	DocumentFileSource(application_base_path + resource_base_path, resource_default_name, resource_minimum_time_between_validity_checks)
attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = resource_source_viewable

# GeneratedTextResource

attributes['com.threecrickets.prudence.GeneratedTextResource.engineManager'] = script_engine_manager
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultEngineName'] = 'python'
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = dynamic_web_default_document
attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = \
	 DocumentFileSource(application_base_path + dynamic_web_base_path, dynamic_web_default_document, dynamic_web_minimum_time_between_validity_checks)
attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = dynamic_web_source_viewable

# Additional runtime attributes

attributes.putAll(runtime_attributes)
