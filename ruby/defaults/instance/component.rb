#
# Prudence Component
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

include Java
import java.lang.Runtime
import java.util.concurrent.Executors
import org.restlet.Component
import com.threecrickets.prudence.DelegatedStatusService
import com.threecrickets.prudence.cache.ChainCache
import com.threecrickets.prudence.cache.HazelcastCache
import 'it.sauronsoftware.cron4j.Scheduler'

#
# Component
#

$component = Component.new
$executable.globals['com.threecrickets.prudence.component'] = $component

$component.context.attributes['com.threecrickets.prudence.version'] = $prudence_version
$component.context.attributes['com.threecrickets.prudence.revision'] = $prudence_revision
$component.context.attributes['com.threecrickets.prudence.flavor'] = $prudence_flavor

#
# Logging
#

$component.log_service.logger_name = 'web-requests'

#
# StatusService
#

$component.status_service = DelegatedStatusService.new

#
# Executor
#

$executor = Executors::new_scheduled_thread_pool(Runtime::runtime.available_processors * 2 + 1)
$component.context.attributes['com.threecrickets.prudence.executor'] = $executor
$tasks = []

#
# Scheduler
#

$scheduler = Scheduler.new
$component.context.attributes['com.threecrickets.prudence.scheduler'] = $scheduler

#
# Cache
#

$cache = ChainCache.new
$cache.caches.add HazelcastCache.new
$component.context.attributes['com.threecrickets.prudence.cache'] = $cache

#
# Predefined Shared Globals
#
# These will be available to your code via application.sharedGlobals.
#

$predefined_shared_globals = {}
