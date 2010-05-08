<?php
//
// Prudence Component
//

import java.lang.Runtime;
import java.util.concurrent.Executors;
import org.restlet.Component;
import com.threecrickets.prudence.util.DelegatedStatusService;
import com.threecrickets.prudence.cache.InProcessMemoryCache

//
// Component
//

$component = new Component();

$component->context->attributes['prudence.version'] = $prudence_version;
$component->context->attributes['prudence.revision'] = $prudence_revision;
$component->context->attributes['prudence.flavor'] = $prudence_flavor;

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

$executor = Executors::newFixedThreadPool(Runtime::getRuntime()->availableProcessors());
$component->context->attributes['prudence.executor'] = $executor;

//
// Cache
//

$component->context->attributes['com.threecrickets.prudence.cache'] = new InProcessMemoryCache();
?>