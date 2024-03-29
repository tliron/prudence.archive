//
// Prudence Instance
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

import java.lang.System
import java.util.logging.LogManager
import java.util.concurrent.Executors
import java.util.concurrent.CopyOnWriteArrayList
import java.io.File
import com.threecrickets.scripturian.document.DocumentFileSource
import com.threecrickets.scripturian.exception.DocumentNotFoundException
import com.threecrickets.prudence.service.ApplicationService

//
// Common
//

commonLibrariesDocumentSource = new DocumentFileSource('common/libraries/', new File(document.source.basePath, 'common/libraries/'), 'default', 'groovy', 5000)
commonFragmentsDocumentSource = new DocumentFileSource('common/web/fragments/', new File(document.source.basePath, 'common/web/fragments/'), 'index', 'groovy', 5000)

commonTasksDocumentSources = new CopyOnWriteArrayList()
commonTasksDocumentSources.add(new DocumentFileSource('common/tasks/', new File(document.source.basePath, 'common/tasks/'), 'default', 'groovy', 5000))
commonHandlersDocumentSources = new CopyOnWriteArrayList()
commonHandlersDocumentSources.add(new DocumentFileSource('common/handlers/', new File(document.source.basePath, 'common/handlers/'), 'default', 'groovy', 5000))

document.librarySources.add(commonLibrariesDocumentSource)

//
// Utilities
//

executeOrDefault = { name, deflt = null ->
	try {
		document.execute(name)
	} catch(DocumentNotFoundException) {
		if(!deflt) {
			deflt = '/defaults/' + name
		}
		document.execute(deflt)
	}
}

//
// Version
//

prudenceVersion = '%VERSION%'
prudenceFlavor = 'Groovy'

//
// Welcome
//

println('Prudence ' + prudenceVersion + ' for ' + prudenceFlavor + '.')
out.flush()

//
// Logging
//

// log4j: This is our actual logging engine
import org.apache.log4j.PropertyConfigurator
try {
	System.setProperty('prudence.logs', document.source.basePath.path + '/logs')
	PropertyConfigurator.configure(document.source.basePath.path + '/configuration/logging.conf')
} catch(x) {}

// JULI: Remove any pre-existing configuration
LogManager.logManager.reset()

// JULI: Bridge to SLF4J, which will use log4j as its engine 
import org.slf4j.bridge.SLF4JBridgeHandler
try {
	SLF4JBridgeHandler.install()
} catch(x) {}

// Set Restlet to use SLF4J, which will use log4j as its engine
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')

// Set Velocity to use log4j
executable.manager.attributes['velocity.runtime.log.logsystem.class'] = 'org.apache.velocity.runtime.log.Log4JLogChute'
executable.manager.attributes['velocity.runtime.log.logsystem.log4j.logger'] = 'velocity'

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

predefinedSharedGlobals.each() { key, value ->
	componentInstance.context.attributes[key] = value
}

//
// Start
//

componentInstance.start()

println('Prudence is up!')
for(server in componentInstance.servers) {
	if(server.address) {
		print('Listening on ' + server.address + ' port ' + server.port + ' for ')
	} else {
		print('Listening on port ' + server.port + ' for ')
	}
	server.protocols.eachWithIndex() { protocol, j ->
		if(j < server.protocols.size() - 1) {
			print(', ')
		}
		print(protocol)
	}
	println('.')
	out.flush()
}

//
// Scheduler
//

scheduler.start()

//
// Tasks
//

fixedExecutor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() * 2 + 1)
if(tasks.size() > 0) {
	futures = []
	startTime = System.currentTimeMillis()
	println('Executing ' + tasks.size() + ' startup tasks...')
	out.flush()
	for(task in tasks) {
		futures.add(fixedExecutor.submit(task))
	}
	for(future in futures) {
		try {
			future.get()
		}
		catch(x) {
		}
	}
	println('Finished all startup tasks in ' + ((System.currentTimeMillis() - startTime) / 1000) + ' seconds.')
	out.flush()
}


for(application in applications) {
	applicationService = new ApplicationService(application)
	applicationService.task(null, '/startup/', null, 'initial', 0, 0, false)
}
