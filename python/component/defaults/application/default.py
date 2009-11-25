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

try:
	document.container.include(applicationBasePath + '/settings')
except FileNotFoundException:
	# Use default application script
	document.container.include('component/defaults/application/settings');

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

attributes['applicationBasePath'] = applicationBasePath
attributes['applicationBaseURL'] = applicationBaseURL

scriptEngineManager = ScriptEngineManager()

# DelegatedResource

attributes['com.threecrickets.prudence.DelegatedResource.engineManager'] = scriptEngineManager
attributes['com.threecrickets.prudence.DelegatedResource.defaultEngineName'] = 'python'
attributes['com.threecrickets.prudence.DelegatedResource.defaultName'] = resourceDefaultName
attributes['com.threecrickets.prudence.DelegatedResource.documentSource'] = \
	DocumentFileSource(File(applicationBasePath + resourceBasePath), resourceDefaultName, resourceMinimumTimeBetweenValidityChecks)
attributes['com.threecrickets.prudence.DelegatedResource.sourceViewable'] = resourceSourceViewable

# GeneratedTextResource

attributes['com.threecrickets.prudence.GeneratedTextResource.engineManager'] = scriptEngineManager
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultEngineName'] = 'python'
attributes['com.threecrickets.prudence.GeneratedTextResource.defaultName'] = dynamicWebDefaultDocument
attributes['com.threecrickets.prudence.GeneratedTextResource.documentSource'] = \
	 DocumentFileSource(File(applicationBasePath + dynamicWebBasePath), dynamicWebDefaultDocument, dynamicWebMinimumTimeBetweenValidityChecks)
attributes['com.threecrickets.prudence.GeneratedTextResource.sourceViewable'] = dynamicWebSourceViewable
