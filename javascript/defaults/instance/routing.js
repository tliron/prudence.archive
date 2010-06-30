//
// Prudence Routing
//

importClass(
	java.io.File,
	java.util.ArrayList,
	com.threecrickets.prudence.util.IoUtil)

// Hosts

executeOrDefault('instance/hosts/')

// Applications

var applications = new ArrayList()
component.context.attributes.put('com.threecrickets.prudence.applications', applications)
var applicationsDir = new File('applications')

var propertiesFile = new File(applicationsDir, 'applications.properties')
var properties = IoUtil.loadProperties(propertiesFile)
var saveProperties = false
var applicationFiles = applicationsDir.listFiles()
for(var i in applicationFiles) {
	var applicationFile = applicationFiles[i]
	if(!applicationFile.directory && applicationFile.name.endsWith('.zip') && properties.getProperty(applicationFile.name, '') != applicationFile.lastModified()) {
		print('Unpacking "' + applicationFile.name + '"...\n')
		IoUtil.unzip(applicationFile, applicationsDir)
		properties.setProperty(applicationFile.name, applicationFile.lastModified())
		saveProperties = true
	}
}
if(saveProperties) {
	IoUtil.saveProperties(properties, propertiesFile)
}

var applicationDirs = applicationsDir.listFiles()
for(var i in applicationDirs) {
	var applicationDir = applicationDirs[i]
	if(applicationDir.directory) {
		var applicationName = applicationDir.name
		var applicationInternalName = applicationDir.name
		var applicationLoggerName = applicationDir.name
		var applicationBasePath = applicationDir.path
		var applicationDefaultURL = '/' + applicationDir.name + '/'
		executeOrDefault(applicationBasePath, 'defaults/application/')
		applications.add(applicationInstance)
	}
}

if(applications.length == 0) {
	print('No applications found. Exiting.\n')
	System.exit(0)
}
