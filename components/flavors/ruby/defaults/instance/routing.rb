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

import java.util.concurrent.CopyOnWriteArrayList
import com.threecrickets.prudence.util.IoUtil

# Hosts

execute_or_default 'instance/hosts/'

# Unzip

$common_dir = java.io.File.new($document.source.base_path, 'common')
$properties_file = java.io.File.new($common_dir, 'common.properties')
$properties = IoUtil::load_properties $properties_file
$save_properties = false
$common_files = $common_dir.list_files
for common_file in $common_files
	last_modified = common_file.last_modified.to_s
	if not common_file.directory and common_file.name =~ /.zip$/ and $properties.get_property(common_file.name, '') != last_modified
		puts "Unpacking common \"#{common_file.name}\"..."
		IoUtil::unzip common_file, $common_dir
		$properties.set_property common_file.name, last_modified
		$save_properties = true
	end
end
if $save_properties
	IoUtil::save_properties $properties, $properties_file
end

$applications_dir = java.io.File.new($document.source.base_path, 'applications')
$properties_file = java.io.File.new($applications_dir, 'applications.properties')
$properties = IoUtil::load_properties $properties_file
$save_properties = false
$applications_files = $applications_dir.list_files
for applications_file in $applications_files
	last_modified = applications_file.last_modified.to_s
	if not applications_file.directory and applications_file.name =~ /.zip$/ and $properties.get_property(applications_file.name, '') != last_modified
		puts "Unpacking applications \"#{applications_file.name}\"..."
		IoUtil::unzip applications_file, $applications_dir
		$properties.set_property applications_file.name, last_modified
		$save_properties = true
	end
end
if $save_properties
	IoUtil::save_properties $properties, $properties_file
end

# Applications

$applications = CopyOnWriteArrayList.new
$component.context.attributes['com.threecrickets.prudence.applications'] = $applications

$application_dirs = $applications_dir.list_files
for application_dir in $application_dirs
	if application_dir.directory and !application_dir.hidden
		$application_name = application_dir.name
		$application_internal_name = application_dir.name
		$application_logger_name = application_dir.name
		$application_base_path = application_dir.path
		$application_default_url = '/' + application_dir.name
		$application_base = 'applications/' + application_dir.name
		execute_or_default $application_base, 'defaults/application'
		$applications.add $application_instance
	end
end

if $applications.length == 0
	puts 'No applications found. Exiting.'
	System::exit 0
end
