<?php
//
// Prudence Component
//

import java.lang.Runtime;
import java.util.concurrent.Executors;
import org.restlet.Component;
import com.threecrickets.prudence.util.DelegatedStatusService;
import com.threecrickets.prudence.cache.InProcessMemoryCache;

global $component, $executor, $prudence_version, $prudence_revision, $prudence_flavor;

//
// Component
//

$component = new Component();

$component->context->attributes['com.threecrickets.prudence.version'] = $prudence_version;
$component->context->attributes['com.threecrickets.prudence.revision'] = $prudence_revision;
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

$executor = Executors::newFixedThreadPool(Runtime::getRuntime()->availableProcessors() * 2 + 1);
$component->context->attributes['com.threecrickets.prudence.executor'] = $executor;

//
// Scheduler
//

$component->context->attributes['com.threecrickets.prudence.scheduler'] = $scheduler;

//
// Cache
//

$component->context->attributes['com.threecrickets.prudence.cache'] = new InProcessMemoryCache();
?>