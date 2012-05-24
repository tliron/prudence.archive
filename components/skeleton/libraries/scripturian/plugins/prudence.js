
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/files/')

importClass(
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException,
	java.io.File)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['prudence']
}

function run(command) {
	switch (String(command.name)) {
		case 'prudence':
			prudence(command)
			break
	}
}

function prudence(command) {
	var prudenceCommand
	if (command.arguments.length > 0) {
		prudenceCommand = String(command.arguments[0])
	}
	else {
		prudenceCommand = 'help'
	}
	
	switch (prudenceCommand) {
		case 'help':
			help(command)
			break
			
		case 'version':
			version(command)
			break
			
		case 'create':
			create(command)
			break
	}
}

function help(command) {
	println('prudence help            Show this help')
	println('prudence version         Show the installed Prudence version')
	println('prudence create [name]   Create a skeleton for a new Prudence application using [name] as the directory name')
}

function version(command) {
	var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getResourceAsProperties('com/threecrickets/prudence/version.conf'))
	println('Version: ' + version.version)
	println('Built: ' + version.built)
}

function create(command) {
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'name')
	}
	var name = command.arguments[1]
	var applicationDir = new File(sincerity.container.getFile('component', 'applications', name))
	if (applicationDir.exists()) {
		throw new CommandException(command, 'The application directory already exists: ' + applicationDir)		
	}
	
	var resources = [
	    'default.js',
		'settings.js',
		'routing.js',
		'mapped/index.d.html',
		'mapped/style/site.zuss',
		'fragments/site/header.html',
		'fragments/site/footer.html',
		'libraries/resources/default.js',
		'libraries/resources/sample.js',
    ]
	
	var base = 'com/threecrickets/prudence/templates/application-skeleton/'
	var token = /\$\{APPLICATION\}/g
	for (var r in resources) {
		var resource = resources[r]
		writeResource(base + resource, new File(applicationDir, resource), token, name)
	}
}

function writeResource(name, file, token, value) {
	var source = new java.io.InputStreamReader(Sincerity.JVM.getResourceAsStream(name), 'UTF-8')
	try {
		file.parentFile.mkdirs()
		var content = new java.io.StringWriter()
		var buffer = Sincerity.JVM.newArray(1024, 'char')
		var length
		while ((length = source.read(buffer)) > 0) {
			content.write(buffer, 0, length)
		}
		content = String(content).replace(token, value)
		var destination = java.io.FileWriter(file)
		try {
			destination.write(content)
		}
		finally {
			destination.close()
		}
	}
	finally {
		source.close()
	}
}
