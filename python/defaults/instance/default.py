#
# Prudence Component
#

from java.lang import System
from java.io import FileNotFoundException
from java.util.logging import LogManager

def execute_or_default(name, default=None):
	try:
		executable.container.execute(name)
	except FileNotFoundException:
		if default is None:
			default = 'defaults/' + name 
		executable.container.execute(default)

tasks = []

#
# Version
#

prudence_version = '1.0'
prudence_revision = '-%REVISION%'
if len(prudence_revision) == 1:
	prudence_revision = ''
prudence_flavor = 'Python'

#
# Welcome
#

print 'Prudence %s%s for %s.' % (prudence_version, prudence_revision, prudence_flavor)

#
# Logging
#

# log4j: This is our actual logging engine
try:
	from org.apache.log4j import PropertyConfigurator
	PropertyConfigurator.configure('configuration/logging.conf')
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
System.setProperty('com.sun.script.velocity.properties', 'configuration/velocity.conf')

#
# Component
#

execute_or_default('instance/component/')

#
# Clients
#

execute_or_default('instance/clients/')

#
# Routing
#

execute_or_default('instance/routing/')

#
# Servers
#

execute_or_default('instance/servers/')

#
# Start
#

component.context.attributes['applications'] = applications
component.start()

#
# Tasks
#

if len(tasks) > 0:
	futures = []
	start_time = System.currentTimeMillis()
	print 'Executing %s tasks...' % len(tasks)
	for task in tasks:
	    futures.append(executor.submit(task))
	for future in futures:
		future.get()
	print 'Finished tasks in %s seconds.' % ((System.currentTimeMillis() - start_time) / 1000.0)
