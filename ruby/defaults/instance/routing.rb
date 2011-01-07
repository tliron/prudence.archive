#
# Prudence Routing
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

import java.util.ArrayList
import com.threecrickets.prudence.util.IoUtil

# Hosts

execute_or_default 'instance/hosts/'

# Applications

$applications = ArrayList.new
$component.context.attributes['com.threecrickets.prudence.applications'] = $applications
$applications_dir = java.io.File.new($document.source.base_path, 'applications')

$properties_file = java.io.File.new($applications_dir, 'applications.properties')
$properties = IoUtil::load_properties $properties_file
$save_properties = false
$application_files = $applications_dir.list_files
for application_file in $application_files
	last_modified = application_file.last_modified.to_s
	if not application_file.directory and application_file.name =~ /.zip$/ and $properties.get_property(application_file.name, '') != last_modified
		puts "Unpacking \"#{application_file.name}\"..."
		IoUtil::unzip application_file, $applications_dir
		$properties.set_property application_file.name, last_modified
		$save_properties = true
	end
end
if $save_properties
	IoUtil::save_properties $properties, $properties_file
end

$application_dirs = $applications_dir.list_files
for application_dir in $application_dirs
	if application_dir.directory and !application_dir.hidden
		$application_name = application_dir.name
		$application_internal_name = application_dir.name
		$application_logger_name = application_dir.name
		$application_base_path = application_dir.path
		$application_default_url = '/' + application_dir.name + '/'
		$application_base = 'applications/' + application_dir.name + '/'
		execute_or_default $application_base, 'defaults/application'
		$applications.add $application_instance
	end
end

if $applications.length == 0
	puts 'No applications found. Exiting.'
	System::exit 0
end
