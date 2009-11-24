#
# Prudence Component
#

from java.lang import System
from java.io import File
from java.util.logging import LogManager

from org.restlet import Component
from org.restlet.data import Protocol

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
# Clients
#

# Required for use of Directory
component.clients.add(Protocol.FILE)

#
# Servers
#

document.container.include('start/servers')

#
# Hosts
#

document.container.include('start/hosts')

#
# Applications
#

applicationDirs = File('applications').listFiles()
for applicationDir in applicationDirs:
	if applicationDir.isDirectory():
		applicationBasePath = applicationDir.path
		applicationBaseURL = '/' + applicationDir.name
		document.container.include(applicationBasePath)

#
# Start
#

component.start()