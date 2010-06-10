//
// Prudence Component
//

importClass(
	java.lang.Runtime,
	java.util.concurrent.Executors,
	org.restlet.Component,
	com.threecrickets.prudence.util.DelegatedStatusService,
	com.threecrickets.prudence.cache.InProcessMemoryCache);

//
// Component
//

var component = new Component();

component.context.attributes.put('prudence.version', prudenceVersion);
component.context.attributes.put('prudence.revision', prudenceRevision);
component.context.attributes.put('prudence.flavor', prudenceFlavor);

//
// Logging
//

component.logService.loggerName = 'web-requests';

//
// StatusService
//

component.statusService = new DelegatedStatusService();

//
// Executor
//

var executor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() * 2 + 1);
component.context.attributes.put('prudence.executor', executor);

//
// Cache
//

component.context.attributes.put('com.threecrickets.prudence.cache', new InProcessMemoryCache());
