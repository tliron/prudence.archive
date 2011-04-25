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

from java.io import File
from java.util.concurrent import CopyOnWriteArrayList
from com.threecrickets.prudence.util import IoUtil

# Hosts

execute_or_default('instance/hosts/')

# Unzip

common_dir = File(document.source.basePath, 'common')
properties_file = File(common_dir, 'common.properties')
properties = IoUtil.loadProperties(properties_file)
save_properties = False
common_files = common_dir.listFiles()
for common_file in common_files:
    last_modified = str(common_file.lastModified())
    if not common_file.directory and common_file.name[-4:] == '.zip' and properties.getProperty(common_file.name, '') != last_modified:
        print 'Unpacking common "' + common_file.name + '"...'
        IoUtil.unzip(common_file, common_dir)
        properties.setProperty(common_file.name, last_modified)
        save_properties = True

if save_properties:
    IoUtil.saveProperties(properties, properties_file)

applications_dir = File(document.source.basePath, 'applications')
properties_file = File(applications_dir, 'applications.properties')
properties = IoUtil.loadProperties(properties_file)
save_properties = False
applications_files = applications_dir.listFiles()
for applications_file in applications_files:
    last_modified = str(applications_file.lastModified())
    if not applications_file.directory and applications_file.name[-4:] == '.zip' and properties.getProperty(applications_file.name, '') != last_modified:
        print 'Unpacking applications "' + applications_file.name + '"...'
        IoUtil.unzip(applications_file, applications_dir)
        properties.setProperty(applications_file.name, last_modified)
        save_properties = True

if save_properties:
    IoUtil.saveProperties(properties, properties_file)

# Applications

applications = component.context.attributes['com.threecrickets.prudence.applications'] = CopyOnWriteArrayList()

application_dirs = applications_dir.listFiles()
for application_dir in application_dirs:
    if application_dir.directory and not application_dir.hidden:
        application_name = application_dir.name
        application_internal_name = application_dir.name;
        application_logger_name = application_dir.name
        application_base_path = application_dir.path
        application_default_url = '/' + application_dir.name
        application_base = 'applications/' + application_dir.name
        execute_or_default(application_base, 'defaults/application/')
        applications.add(application_instance)

if len(applications) == 0:
    print 'No applications found. Exiting.'
    System.exit(0)
