#
# Prudence Routing
#

import java.util.ArrayList

# Hosts

include_or_default 'instance/hosts'

# Applications

$applications = ArrayList.new
$component.context.attributes['applications'] = $applications
$application_dirs = java.io.File.new('applications').list_files
for application_dir in $application_dirs
	if application_dir.is_directory
		$application_name = application_dir.name
		$application_internal_name = application_dir.name
		$application_logger_name = application_dir.name
		$application_base_path = application_dir.path
		$application_default_url = '/' + application_dir.name + '/'
		include_or_default $application_base_path, 'defaults/application'
		$applications.add $application
	end
end

if $applications.length == 0
	puts 'No applications found. Exiting.'
	System::exit 0
end
