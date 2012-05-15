<?php
//
// Prudence Component
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

import java.lang.Runtime;
import java.util.concurrent.Executors;
import org.restlet.Component;
import com.threecrickets.prudence.DelegatedStatusService;
import com.threecrickets.prudence.cache.ChainCache;
import com.threecrickets.prudence.cache.HazelcastCache;
import it.sauronsoftware.cron4j.Scheduler;

global $executable;
global $component, $executor, $scheduler, $prudence_version, $prudence_flavor, $predefined_shared_globals;

//
// Component
//

$component = new Component();
$executable->globals['com.threecrickets.prudence.component'] = $component;

$component->context->attributes['com.threecrickets.prudence.version'] = $prudence_version;
$component->context->attributes['com.threecrickets.prudence.flavor'] = $prudence_flavor;

//
// Logging
//

$component->logService->loggerName = 'web-requests';

//
// StatusService
//

$component->statusService = new DelegatedStatusService();

//
// Executor
//

$executor = Executors::newScheduledThreadPool(Runtime::getRuntime()->availableProcessors() * 2 + 1);
$component->context->attributes['com.threecrickets.prudence.executor'] = $executor;
$tasks = array();

//
// Scheduler
//

$scheduler = new Scheduler();
$component->context->attributes['com.threecrickets.prudence.scheduler'] = $scheduler;

//
// Cache
//

$cache = new ChainCache();
$cache->caches->add(new HazelcastCache());
$component->context->attributes['com.threecrickets.prudence.cache'] = $cache;

//
// Predefined Shared Globals
//
// These will be available to your code via application.sharedGlobals.
//

$predefined_shared_globals = array();
?>