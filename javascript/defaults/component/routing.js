//
// Prudence Routing
//

importClass(
	java.io.File,
	java.util.ArrayList);

// Hosts

includeOrDefault('component/hosts');

// Applications

var applications = new ArrayList();
component.context.attributes.put('applications', applications);
var applicationDirs = new File('applications').listFiles();
for(var i in applicationDirs) {
	var applicationDir = applicationDirs[i]; 
	if(applicationDir.isDirectory()) {
		var applicationName = applicationDir.name;
		var applicationInternalName = applicationDir.name;
		var applicationLoggerName = applicationDir.name;
		var applicationBasePath = applicationDir.path;
		var applicationDefaultURL = '/' + applicationDir.name + '/';
		includeOrDefault(applicationBasePath, 'defaults/application');
		applications.add(application);
	}
}

if(applications.length == 0) {
	print('No applications found. Exiting.\n');
	System.exit(0);
}
