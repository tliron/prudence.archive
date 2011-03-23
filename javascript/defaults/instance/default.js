//
// Prudence Instance
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
	java.lang.System,
	java.util.logging.LogManager,
	java.io.File,
	com.threecrickets.scripturian.document.DocumentFileSource,
	com.threecrickets.scripturian.exception.DocumentNotFoundException,
	com.threecrickets.prudence.service.ApplicationService)
 
document.librarySources.add(new DocumentFileSource(new File(document.source.basePath, 'libraries/javascript'), 'default', 'js', 5000))

function executeOrDefault(name, def) {
	try {
		document.execute(name)
	} catch(e if e.javaException instanceof DocumentNotFoundException) {
		if(!def) {
			def = '/defaults/' + name
		}
		document.execute(def)
	}
}

//
// Version
//

var prudenceVersion = '1.1'
var prudenceRevision = '-%REVISION%'
if(prudenceRevision.length == 1) {
	prudenceRevision = ''
}
var prudenceFlavor = 'JavaScript'

//
// Welcome
//

print('Prudence ' + prudenceVersion + prudenceRevision + ' for ' + prudenceFlavor + '.\n')

//
// Logging
//

// log4j: This is our actual logging engine
try {
	importClass(org.apache.log4j.PropertyConfigurator)
	System.setProperty('prudence.logs', document.source.basePath.path + '/logs')
	PropertyConfigurator.configure(document.source.basePath.path + '/configuration/logging.conf')
} catch(x) {}

// JULI: Remove any pre-existing configuration
LogManager.logManager.reset()

// JULI: Bridge to SLF4J, which will use log4j as its engine 
try {
	importClass(org.slf4j.bridge.SLF4JBridgeHandler)
	SLF4JBridgeHandler.install()
} catch(x) {}

// Set Restlet to use SLF4J, which will use log4j as its engine
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')

// Set Velocity to use log4j
executable.manager.attributes.put('velocity.runtime.log.logsystem.class', 'org.apache.velocity.runtime.log.Log4JLogChute')
executable.manager.attributes.put('velocity.runtime.log.logsystem.log4j.logger', 'velocity')

// Set spymemcached to use log4j
System.setProperty('net.spy.log.LoggerImpl', 'net.spy.memcached.compat.log.Log4JLogger')

//
// Configuration
//

// Hazelcast
System.setProperty('hazelcast.config', 'configuration/hazelcast.conf')

//
// Component
//

executeOrDefault('instance/component/')

//
// Clients
//

executeOrDefault('instance/clients/')

//
// Routing
//

executeOrDefault('instance/routing/')

//
// Servers
//

executeOrDefault('instance/servers/')

//
// Predefined Shared Globals
//

for(var key in predefinedSharedGlobals) {
	component.context.attributes.put(key, predefinedSharedGlobals[key])
}

//
// Start
//

component.start()

print('Prudence is up!\n')
for(var i = 0; i < component.servers.size(); i++) {
	var server = component.servers.get(i)
	if(server.address) {
		print('Listening on ' + server.address + ' port ' + server.port + ' for ')
	} else {
		print('Listening on port ' + server.port + ' for ')
	}
	for(var j = 0; j < server.protocols.size(); j++) {
		var protocol = server.protocols.get(j)
		if(j < server.protocols.size() - 1) {
			print(', ')
		}
		print(protocol)
	}
	print('.\n')
}

//
// Scheduler
//

scheduler.start()

//
// Tasks
//

var fixedExecutor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() * 2 + 1)
if(tasks.length > 0) {
	var futures = []
	var startTime = System.currentTimeMillis()
	print('Executing ' + tasks.length + ' startup tasks...\n')
	for(var i in tasks) {
		var task = tasks[i]
		futures.push(fixedExecutor.submit(task))
	}
	for(var i in futures) {
		var future = futures[i]
		future.get()
	}
	print('Finished all startup tasks in ' + ((System.currentTimeMillis() - startTime) / 1000) + ' seconds.\n')
}

for(var i = applications.iterator(); i.hasNext(); ) {
	var applicationService = new ApplicationService(i.next())
	applicationService.task('/startup/', 'initial', 0, 0, false)
}
