#
# Prudence Component
#

from java.lang import System
from java.io import File, FileNotFoundException
from java.util.logging import LogManager

from org.restlet import Component
from org.restlet.data import Protocol

#
# Welcome
#

print 'Prudence 1.0 for Python.'

#
# Component
#

component = Component()

#
# Logging
#

# log4j: This is our actual logging engine
try:
	from org.apache.log4j import PropertyConfigurator
	PropertyConfigurator.configure('conf/logging.conf')
except:
	raise

# JULI: Remove any pre-existing configuration
LogManager.getLogManager().reset()

# JULI: Bridge to SLF4J, which will use log4j as its engine 
try:
	from org.slf4j.bridge import SLF4JBridgeHandler
	SLF4JBridgeHandler.install()
except:
	raise

# Set Restlet to use SLF4J, which will use log4j as its engine
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')

# Velocity logging
System.setProperty('com.sun.script.velocity.properties', 'conf/velocity.conf')

# Web requests
component.logService.loggerName = 'web-requests'

#
# Hosts
#

document.container.include('component/hosts')

#
# Applications
#

applications = component.context.attributes['applications'] = []
application_dirs = File('applications').listFiles()
for application_dir in application_dirs:
	if application_dir.isDirectory():
		application_name = application_dir.name
		application_logger_name = application_dir.name
		application_base_path = application_dir.path
		application_default_url = '/' + application_dir.name + '/'
		try:
			document.container.include(application_base_path)
		except FileNotFoundException:
			# Use default application script
			document.container.include('component/defaults/application');
		applications.append(application)

if len(applications) == 0:
	print 'No applications found. Exiting.'
	System.exit(0)

#
# Clients
#

# Required for use of Directory
component.clients.add(Protocol.FILE)

#
# Servers
#

document.container.include('component/servers')

#
# Start
#

component.context.attributes['applications'] = applications
component.start()
