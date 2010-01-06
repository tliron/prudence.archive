#
# Prudence Routing
#

from java.io import File

# Hosts

include_or_default('component/hosts')

# Applications

applications = component.context.attributes['applications'] = []
application_dirs = File('applications').listFiles()
for application_dir in application_dirs:
    if application_dir.isDirectory():
        application_name = application_dir.name
        application_internal_name = application_dir.name;
        application_logger_name = application_dir.name
        application_base_path = application_dir.path
        application_default_url = '/' + application_dir.name + '/'
        include_or_default(application_base_path, 'defaults/application')
        applications.append(application)

if len(applications) == 0:
    print 'No applications found. Exiting.'
    System.exit(0)
