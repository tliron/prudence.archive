//
// Prudence Component
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
	java.lang.Runtime,
	java.util.concurrent.Executors,
	org.restlet.Component,
	com.threecrickets.prudence.DelegatedStatusService,
	com.threecrickets.prudence.cache.ChainCache,
	com.threecrickets.prudence.cache.HazelcastCache,
	Packages.it.sauronsoftware.cron4j.Scheduler)

//
// Component
//

var component = new Component()
executable.globals.put('com.threecrickets.prudence.component', component)

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

var executor = Executors.newScheduledThreadPool(Runtime.runtime.availableProcessors() * 2 + 1)
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

var cache = new ChainCache()
cache.caches.add(new HazelcastCache())
component.context.attributes.put('com.threecrickets.prudence.cache', cache)

//
// Predefined Shared Globals
//
// These will be available to your code via application.sharedGlobals.
//

var predefinedSharedGlobals = {}
