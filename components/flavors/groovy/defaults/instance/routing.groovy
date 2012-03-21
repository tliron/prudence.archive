//
// Prudence Routing
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import com.threecrickets.prudence.util.IoUtil

// Hosts

executeOrDefault('instance/hosts/')

// Unzip

commonDir = new File(document.source.basePath, 'common')
propertiesFile = new File(commonDir, 'common.properties')
properties = IoUtil.loadProperties(propertiesFile)
saveProperties = false
commonFiles = commonDir.listFiles()
for(commonFile in commonFiles) {
	lastModified = commonFile.lastModified().toString()
	if(!commonFile.directory && commonFile.name.endsWith('.zip') && properties.getProperty(commonFile.name, '') != lastModified) {
		println('Unpacking common "' + commonFile.name + '"...')
		IoUtil.unzip(commonFile, commonDir)
		properties.setProperty(commonFile.name, lastModified)
		saveProperties = true
	}
}
if(saveProperties) {
	IoUtil.saveProperties(properties, propertiesFile)
}

applicationsDir = new File(document.source.basePath, 'applications')
propertiesFile = new File(applicationsDir, 'applications.properties')
properties = IoUtil.loadProperties(propertiesFile)
saveProperties = false
applicationsFiles = applicationsDir.listFiles()
for(applicationsFile in applicationsFiles) {
	lastModified = applicationsFile.lastModified().toString()
	if(!applicationsFile.directory && applicationsFile.name.endsWith('.zip') && properties.getProperty(applicationsFile.name, '') != lastModified) {
		println('Unpacking applications "' + applicationsFile.name + '"...')
		IoUtil.unzip(applicationsFile, applicationsDir)
		properties.setProperty(applicationsFile.name, lastModified)
		saveProperties = true
	}
}
if(saveProperties) {
	IoUtil.saveProperties(properties, propertiesFile)
}

// Applications

applications = new CopyOnWriteArrayList()
componentInstance.context.attributes['com.threecrickets.prudence.applications'] = applications

applicationDirs = applicationsDir.listFiles()
for(applicationDir in applicationDirs) {
	if(applicationDir.directory && !applicationDir.hidden) {
		applicationName = applicationDir.name
		applicationInternalName = applicationDir.name
		applicationLoggerName = applicationDir.name
		applicationBasePath = applicationDir.path
		applicationDefaultURL = '/' + applicationDir.name
		applicationBase = 'applications/' + applicationDir.name
		executeOrDefault(applicationBase, 'defaults/application/')
		applications.add(applicationInstance)
	}
}

if(applications.size() == 0) {
	println('No applications found. Exiting.')
	out.flush()
	System.exit(0)
}
