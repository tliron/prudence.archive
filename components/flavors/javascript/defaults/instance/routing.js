//
// Prudence Routing
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

importClass(
	java.io.File,
	java.util.concurrent.CopyOnWriteArrayList,
	com.threecrickets.prudence.util.IoUtil)

// Hosts

executeOrDefault('instance/hosts/')

// Unzip

var commonDir = new File(document.source.basePath, 'common')
var propertiesFile = new File(commonDir, 'common.properties')
var properties = IoUtil.loadProperties(propertiesFile)
var saveProperties = false
var commonFiles = commonDir.listFiles()
for(var i in commonFiles) {
	var commonFile = commonFiles[i]
	if(!commonFile.directory && commonFile.name.endsWith('.zip') && properties.getProperty(commonFile.name, '') != commonFile.lastModified()) {
		print('Unpacking common "' + commonFile.name + '"...\n')
		IoUtil.unzip(commonFile, commonDir)
		properties.setProperty(commonFile.name, commonFile.lastModified())
		saveProperties = true
	}
}
if(saveProperties) {
	IoUtil.saveProperties(properties, propertiesFile)
}

var applicationsDir = new File(document.source.basePath, 'applications')
propertiesFile = new File(applicationsDir, 'applications.properties')
properties = IoUtil.loadProperties(propertiesFile)
saveProperties = false
var applicationsFiles = applicationsDir.listFiles()
for(var i in applicationsFiles) {
	var applicationsFile = applicationsFiles[i]
	if(!applicationsFile.directory && applicationsFile.name.endsWith('.zip') && properties.getProperty(applicationsFile.name, '') != applicationsFile.lastModified()) {
		print('Unpacking applications "' + applicationsFile.name + '"...\n')
		IoUtil.unzip(applicationsFile, applicationsDir)
		properties.setProperty(applicationsFile.name, applicationsFile.lastModified())
		saveProperties = true
	}
}
if(saveProperties) {
	IoUtil.saveProperties(properties, propertiesFile)
}

// Applications

var applications = new CopyOnWriteArrayList()
component.context.attributes.put('com.threecrickets.prudence.applications', applications)

var applicationDirs = applicationsDir.listFiles()
for(var i in applicationDirs) {
	var applicationDir = applicationDirs[i]
	if(applicationDir.directory && !applicationDir.hidden) {
		var applicationName = applicationDir.name
		var applicationInternalName = applicationDir.name
		var applicationLoggerName = applicationDir.name
		var applicationBasePath = applicationDir.path
		var applicationDefaultURL = '/' + applicationDir.name
		var applicationBase = 'applications/' + applicationDir.name
		executeOrDefault(applicationBase, 'defaults/application/')
		applications.add(applicationInstance)
	}
}

if(applications.length == 0) {
	print('No applications found. Exiting.\n')
	System.exit(0)
}
