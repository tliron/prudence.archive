//
// Prudence Component
//

importClass(
	java.lang.Runtime,
	java.util.concurrent.Executors,
	org.restlet.Component,
	com.threecrickets.prudence.util.DelegatedStatusService,
	com.threecrickets.prudence.cache.InProcessMemoryCache,
	Packages.it.sauronsoftware.cron4j.Scheduler)

//
// Component
//

var component = new Component()

component.context.attributes.put('com.threecrickets.prudence.version', prudenceVersion)
component.context.attributes.put('com.threecrickets.prudence.revision', prudenceRevision)
component.context.attributes.put('com.threecrickets.prudence.flavor', prudenceFlavor)

//
// Logging
//

component.logService.loggerName = 'web-requests'

//
// StatusService
//

component.statusService = new DelegatedStatusService()

//
// Executor
//

var executor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() * 2 + 1)
component.context.attributes.put('com.threecrickets.prudence.executor', executor)
var tasks = []

//
// Scheduler
//

var scheduler = new Scheduler()
component.context.attributes.put('com.threecrickets.prudence.scheduler', scheduler)

//
// Cache
//

component.context.attributes.put('com.threecrickets.prudence.cache', new InProcessMemoryCache())
