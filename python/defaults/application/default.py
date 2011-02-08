#
# Prudence Application
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

from org.restlet.data import Reference, MediaType
from com.threecrickets.prudence import DelegatedStatusService, ApplicationTaskCollector
from com.threecrickets.prudence.util import LoggingUtil
from com.threecrickets.prudence.service import ApplicationService

#
# Settings
#

execute_or_default(application_base + '/settings/', 'defaults/application/settings/')

#
# Application
#

execute_or_default(application_base + '/application/', 'defaults/application/application/')

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

execute_or_default(application_base + '/routing/', 'defaults/application/routing/')

#
# Logging
#

application_instance.context.setLogger(LoggingUtil.getRestletLogger(application_logger_name))

#
# Predefined Globals
#

application_globals.putAll(predefined_globals)

#
# Handlers
#

handlers_document_source = DocumentFileSource(application_base + handlers_base_path, application_base_path + handlers_base_path, documents_default_name, 'py', minimum_time_between_validity_checks)
application_globals['com.threecrickets.prudence.DelegatedHandler.documentSource'] = handlers_document_source

#
# Tasks
#

tasks_document_source = DocumentFileSource(application_base + tasks_base_path, application_base_path + tasks_base_path, documents_default_name, 'py', minimum_time_between_validity_checks)
application_globals['com.threecrickets.prudence.ApplicationTask.documentSource'] = tasks_document_source

scheduler.addTaskCollector(ApplicationTaskCollector(File(application_base_path + '/crontab'), application_instance))

#
# Common Configurations
#

def configure_common(prefix):
    application_globals[prefix + '.languageManager'] = language_manager
    application_globals[prefix + '.defaultName'] = documents_default_name
    application_globals[prefix + '.defaultLanguageTag'] = 'python'
    application_globals[prefix + '.librariesDocumentSource'] = libraries_document_source
    application_globals[prefix + '.commonLibrariesDocumentSource'] = common_libraries_document_source
    application_globals[prefix + '.fileUploadSizeThreshold'] = file_upload_size_threshold
    application_globals[prefix + '.sourceViewable'] = source_viewable

configure_common('com.threecrickets.prudence.GeneratedTextResource')
configure_common('com.threecrickets.prudence.DelegatedResource')
configure_common('com.threecrickets.prudence.DelegatedHandler')
configure_common('com.threecrickets.prudence.ApplicationTask')

#
# ApplicationService
#

application_service = ApplicationService(application_instance)
