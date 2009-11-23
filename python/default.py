
from java.lang import System, ClassLoader
from java.io import File
from java.util.logging import LogManager

from javax.script import ScriptEngineManager
from org.restlet import Application, Component
from org.restlet.resource import Directory
from org.restlet.routing import Router, Redirector, Template
from org.restlet.data import Protocol, Reference

from com.threecrickets.prudence import DelegatedResource, GeneratedTextResource
from com.threecrickets.scripturian.file import DocumentFileSource

document.container.include('conf/prudence')

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

#
# Component and Server
#

component = Component()
component.servers.add(Protocol.HTTP, serverPort)
component.clients.add(Protocol.FILE)
component.logService.loggerName = componentWebLoggerName

#
# Application
#

application = Application()
application.name = applicationName
application.description = applicationDescription
application.author = applicationAuthor
application.owner = applicationOwner
application.statusService.homeRef = Reference(applicationHomeURL)
application.statusService.contactEmail = applicationContactEmail

# The context will not be created until we attach the application
component.defaultHost.attach(application)
application.context.setLogger(applicationLoggerName)

#
# Router
#

router = Router(application.context)
application.inboundRoot = router
document.container.include('conf/routing')

#
# Attributes
#

attributes = application.context.attributes
scriptEngineManager = ScriptEngineManager()

# DelegatedResource

attributes['com.threecrickets.prudence.DelegatedResource.scriptEngineManager'] = scriptEngineManager
attributes['com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName'] = 'python'
attributes['com.threecrickets.prudence.DelegatedResource.extension'] = resourceExtension
attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = resourceDefaultName
attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = \
	DocumentFileSource(File(resourceBasePath), resourceDefaultName, resourceExtension, resourceMinimumTimeBetweenValidityChecks)
attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = resourceSourceViewable

# GeneratedTextResource

attributes['com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager'] = scriptEngineManager
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName'] = 'python'
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = dynamicWebDefaultDocument
attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = \
	 DocumentFileSource(File(dynamicWebBasePath), dynamicWebDefaultDocument, dynamicWebExtension, dynamicWebMinimumTimeBetweenValidityChecks)
attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = dynamicWebSourceViewable

#
# Start
#

component.start()

print('Prudence started at port %s' % serverPort)
