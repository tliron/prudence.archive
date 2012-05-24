
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/files/')

importClass(
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException,
	java.io.File,
	java.io.FileReader,
	java.io.FileWriter,
	java.io.StringWriter)

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
	command.parse = true

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
	println('prudence help                         Show this help')
	println('prudence version                      Show the installed Prudence version')
	println('prudence create [name] [[template]]   Create a skeleton for a new Prudence application using [name] as the directory name')
}

function version(command) {
	var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getReinputAsProperties('com/threecrickets/prudence/version.conf'))
	println('Version: ' + version.version)
	println('Built: ' + version.built)
}

function create(command) {
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'name', '[template=application]')
	}
	var name = command.arguments[1]
	var templateName = 'application'
	if (command.arguments.length > 2) {
		templateName = command.arguments[2]
	}
	var force = command.switches.contains('force')
	
	var applicationDir = new File(sincerity.container.getFile('component', 'applications', name))
	if (!force && applicationDir.exists()) {
		throw new CommandException(command, 'The application directory already exists: ' + applicationDir)		
	}
	
	var templateDir = new File(sincerity.container.getFile('component', 'templates', templateName))
	if (!templateDir.exists()) {
		throw new CommandException(command, 'The template does not exist: ' + templateDir)		
	}
	
	copy(templateDir, applicationDir, /\$\{APPLICATION\}/g, name)
}

function copy(source, destination, token, value) {
	if (source.directory) {
		var sourceFiles = source.listFiles()
		for (var f in sourceFiles) {
			sourceFile = sourceFiles[f]
			copy(sourceFile, new File(destination, sourceFile.name), token, value)
		}
	}
	else {
		destination.parentFile.mkdirs()
		var content = Sincerity.Files.loadText(source, 'UTF-8')
		content = String(content).replace(token, value)
		var output = FileWriter(destination)
		try {
			output.write(content)
		}
		finally {
			output.close()
		}
	}
}
