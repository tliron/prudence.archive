#
# Prudence Component
#

from java.lang import System, Runtime
from java.io import FileNotFoundException
from java.util.logging import LogManager
from java.util.concurrent import Executors

from org.restlet import Component
from com.threecrickets.prudence.util import DelegatedStatusService, MessageTask

def include_or_default(name, default=None):
	try:
		document.container.include(name)
	except FileNotFoundException:
		if default is None:
			default = 'defaults/' + name 
		document.container.include(default)

tasks = []

#
# Welcome
#

revision = '%REVISION%'
if revision[0] == '%':
	revision = ''
else:
	revision = '-' + revision
print 'Prudence 1.0%s for Python.' % revision

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

# Web requests
component.logService.loggerName = 'web-requests'

#
# StatusService
#

component.statusService = DelegatedStatusService()

#
# Executor
#

executor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors())
component.context.attributes['executor'] = executor

#
# Clients
#

include_or_default('instance/clients')

#
# Routing
#

include_or_default('instance/routing')

#
# Servers
#

include_or_default('instance/servers')

#
# Start
#

component.context.attributes['applications'] = applications
component.start()

#
# Tasks
#

if len(tasks) > 0:
	executor.submit(MessageTask(component.context, 'Executing %s tasks...' % len(tasks)))
	for task in tasks:
	    executor.submit(task)
	executor.submit(MessageTask(component.context, 'Finished tasks.'))
