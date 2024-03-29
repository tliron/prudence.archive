#
# Prudence Instance
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.gnu.org/copyleft/lesser.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

from java.lang import System
from java.util.logging import LogManager
from java.util.concurrent import Executors, CopyOnWriteArrayList
from java.io import File
from com.threecrickets.scripturian.document import DocumentFileSource
from com.threecrickets.scripturian.exception import DocumentNotFoundException
from com.threecrickets.prudence.service import ApplicationService

#
# Common
#

common_libraries_document_source = DocumentFileSource('common/libraries/', File(document.source.basePath, 'common/libraries/'), 'default', 'py', 5000)
common_fragments_document_source = DocumentFileSource('common/web/fragments/', File(document.source.basePath, 'common/web/fragments/'), 'index', 'py', 5000)

common_tasks_document_sources = CopyOnWriteArrayList()
common_tasks_document_sources.add(DocumentFileSource('common/tasks/', File(document.source.basePath, 'common/tasks/'), 'default', 'py', 5000))
common_handlers_document_sources = CopyOnWriteArrayList()
common_handlers_document_sources.add(DocumentFileSource('common/handlers/', File(document.source.basePath, 'common/handlers/'), 'default', 'py', 5000))

document.librarySources.add(common_libraries_document_source)

#
# Utilities
#

def execute_or_default(name, default=None):
	try:
		document.execute(name)
	except DocumentNotFoundException:
		if default is None:
			default = '/defaults/' + name 
		document.execute(default)

#
# Version
#

prudence_version = '%VERSION%'
prudence_flavor = 'Python'

#
# Welcome
#

print 'Prudence %s for %s.' % (prudence_version, prudence_flavor)

#
# Logging
#

# log4j: This is our actual logging engine
try:
	from org.apache.log4j import PropertyConfigurator
	System.setProperty('prudence.logs', document.source.basePath.path + '/logs')
	PropertyConfigurator.configure(document.source.basePath.path + '/configuration/logging.conf')
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

# Set Velocity to use log4j
executable.manager.attributes['velocity.runtime.log.logsystem.class'] = 'org.apache.velocity.runtime.log.Log4JLogChute'
executable.manager.attributes['velocity.runtime.log.logsystem.log4j.logger'] = 'velocity'

# Set spymemcached to use log4j
System.setProperty('net.spy.log.LoggerImpl', 'net.spy.memcached.compat.log.Log4JLogger')

#
# Configuration
#

# Hazelcast
System.setProperty('hazelcast.config', 'configuration/hazelcast.conf')

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
# Predefined Shared Globals
#

component.context.attributes.putAll(predefined_shared_globals)

#
# Start
#

component.start()

print 'Prudence is up!'
for i in range(len(component.servers)):
	server = component.servers[i]
	if server.address:
		sys.stdout.write('Listening on %s port %s for ' % (server.address, server.port))
	else:
		sys.stdout.write('Listening on port %s for ' % server.port)
	for j in range(len(server.protocols)):
		protocol = server.protocols[j]
		if j < len(server.protocols) - 1:
			sys.stdout.write(', ')
		sys.stdout.write(str(protocol))
	print '.'

#
# Scheduler
#

scheduler.start()

#
# Tasks
#

fixed_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1)
if len(tasks) > 0:
	futures = []
	start_time = System.currentTimeMillis()
	print 'Executing %s startup tasks...' % len(tasks)
	for task in tasks:
	    futures.append(fixed_executor.submit(task))
	for future in futures:
		try:
			future.get()
		except:
			pass
	print 'Finished all startup tasks in %s seconds.' % ((System.currentTimeMillis() - start_time) / 1000.0)

for application in applications:
	applicationService = ApplicationService(application)
	applicationService.task(None, '/startup/', None, 'initial', 0, 0, False)
