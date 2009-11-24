#
# Prudence Application
#

from java.io import File, FileNotFoundException
from javax.script import ScriptEngineManager
from org.restlet import Application
from org.restlet.data import Reference
from com.threecrickets.scripturian.file import DocumentFileSource

#
# Settings
#

document.container.include(applicationBasePath + '/settings')

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

#
# Routing
#

try:
	document.container.include(applicationBasePath + '/routing')
except FileNotFoundException:
	# Use default application script
	document.container.include('component/defaults/application/routing');

#
# Logging
#

application.context.setLogger(applicationLoggerName)

#
# Configuration
#

attributes = application.context.attributes

scriptEngineManager = ScriptEngineManager()

# DelegatedResource

attributes.put('com.threecrickets.prudence.DelegatedResource.scriptEngineManager', scriptEngineManager)
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultScriptEngineName', 'python')
attributes.put('com.threecrickets.prudence.DelegatedResource.extension', resourceExtension)
attributes.put('com.threecrickets.prudence.DelegatedResource.defaultName', resourceDefaultName)
attributes.put('com.threecrickets.prudence.DelegatedResource.documentSource', \
	DocumentFileSource(File(applicationBasePath + resourceBasePath), resourceDefaultName, resourceExtension, resourceMinimumTimeBetweenValidityChecks))
attributes.put('com.threecrickets.prudence.DelegatedResource.sourceViewable', resourceSourceViewable)

# GeneratedTextResource

attributes.put('com.threecrickets.prudence.GeneratedTextResource.scriptEngineManager', scriptEngineManager)
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultScriptEngineName', 'python')
attributes.put('com.threecrickets.prudence.GeneratedTextResource.defaultName', dynamicWebDefaultDocument)
attributes.put('com.threecrickets.prudence.GeneratedTextResource.documentSource', \
	 DocumentFileSource(File(applicationBasePath + dynamicWebBasePath), dynamicWebDefaultDocument, dynamicWebExtension, dynamicWebMinimumTimeBetweenValidityChecks))
attributes.put('com.threecrickets.prudence.GeneratedTextResource.sourceViewable', dynamicWebSourceViewable)
