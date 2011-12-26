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

import org.restlet.data.Reference
import org.restlet.data.MediaType
import com.threecrickets.prudence.DelegatedStatusService
import com.threecrickets.prudence.ApplicationTaskCollector
import com.threecrickets.prudence.util.LoggingUtil
import com.threecrickets.prudence.service.ApplicationService

#
# Settings
#

execute_or_default $application_base + '/settings/', '/defaults/application/settings/'

#
# Application
#

execute_or_default $application_base + '/application/', '/defaults/application/application/'

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

execute_or_default $application_base + '/routing/', '/defaults/application/routing/'

#
# Logging
#

$application_instance.context.logger = LoggingUtil::get_restlet_logger($application_logger_name)

#
# Predfined Globals
#

for key in $predefined_globals.keys
	$application_globals[key] = $predefined_globals[key]
end

#
# Handlers
#

$handlers_document_source = DocumentFileSource.new($application_base + $handlers_base_path, $application_base_path + $handlers_base_path, $documents_default_name, 'rb', $minimum_time_between_validity_checks)
$application_globals['com.threecrickets.prudence.DelegatedHandler.documentSource'] = $handlers_document_source
$application_globals['com.threecrickets.prudence.DelegatedHandler.extraDocumentSources'] = $common_handlers_document_sources

#
# Tasks
#

$tasks_document_source = DocumentFileSource.new($application_base + $tasks_base_path, $application_base_path + $tasks_base_path, $documents_default_name, 'rb', $minimum_time_between_validity_checks)
$application_globals['com.threecrickets.prudence.ApplicationTask.documentSource'] = $tasks_document_source
$application_globals['com.threecrickets.prudence.ApplicationTask.extraDocumentSources'] = $common_tasks_document_sources

$scheduler.add_task_collector ApplicationTaskCollector.new(java.io.File.new($application_base_path + '/crontab'), $application_instance)

#
# Common Configurations
#

$file_upload_directory = java.io.File.new($application_base_path + $file_upload_base_path)
def configure_common(prefix)
	$application_globals[prefix + '.languageManager'] = $language_manager
	$application_globals[prefix + '.defaultName'] = $documents_default_name
	$application_globals[prefix + '.defaultLanguageTag'] = 'ruby'
	$application_globals[prefix + '.libraryDocumentSources'] = $libraries_document_sources
	$application_globals[prefix + '.fileUploadDirectory'] = $file_upload_directory
	$application_globals[prefix + '.fileUploadSizeThreshold'] = $file_upload_size_threshold
	$application_globals[prefix + '.sourceViewable'] = $source_viewable
end

configure_common 'com.threecrickets.prudence.GeneratedTextResource'
configure_common 'com.threecrickets.prudence.DelegatedResource'
configure_common 'com.threecrickets.prudence.DelegatedHandler'
configure_common 'com.threecrickets.prudence.ApplicationTask'

#
# ApplicationService
#

$application_service = ApplicationService.new $application_instance
