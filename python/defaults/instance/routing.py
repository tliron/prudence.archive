#
# Prudence Routing
#

from java.io import File
from com.threecrickets.prudence.util import IoUtil

# Hosts

execute_or_default('instance/hosts/')

# Applications

applications = component.context.attributes['com.threecrickets.prudence.applications'] = []
applications_dir = File(document.source.basePath, 'applications')

properties_file = File(applications_dir, 'applications.properties')
properties = IoUtil.loadProperties(properties_file)
save_properties = False
application_files = applications_dir.listFiles()
for application_file in application_files:
    last_modified = str(application_file.lastModified())
    if not application_file.directory and application_file.name[-4:] == '.zip' and properties.getProperty(application_file.name, '') != last_modified:
        print 'Unpacking "' + application_file.name + '"...'
        IoUtil.unzip(application_file, applications_dir)
        properties.setProperty(application_file.name, last_modified)
        save_properties = True

if save_properties:
    IoUtil.saveProperties(properties, properties_file)

application_dirs = applications_dir.listFiles()
for application_dir in application_dirs:
    if application_dir.isDirectory():
        application_name = application_dir.name
        application_internal_name = application_dir.name;
        application_logger_name = application_dir.name
        application_base_path = application_dir.path
        application_default_url = '/' + application_dir.name + '/'
        application_base = 'applications/' + application_dir.name + '/'
        execute_or_default(application_base, 'defaults/application/')
        applications.append(application_instance)

if len(applications) == 0:
    print 'No applications found. Exiting.'
    System.exit(0)
