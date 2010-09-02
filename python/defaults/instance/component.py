#
# Prudence Component
#

from java.lang import Runtime
from java.util.concurrent import Executors
from org.restlet import Component
from com.threecrickets.prudence import DelegatedStatusService
from com.threecrickets.prudence.cache import InProcessMemoryCache
from it.sauronsoftware.cron4j import Scheduler

#
# Component
#

component = Component()

component.context.attributes['com.threecrickets.prudence.version'] = prudence_version
component.context.attributes['com.threecrickets.prudence.revision'] = prudence_revision
component.context.attributes['com.threecrickets.prudence.flavor'] = prudence_flavor

#
# Logging
#

component.logService.loggerName = 'web-requests'

#
# StatusService
#

component.statusService = DelegatedStatusService()

#
# Executor
#

executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1)
component.context.attributes['com.threecrickets.prudence.executor'] = executor
tasks = []

#
# Scheduler
#

scheduler = Scheduler()
component.context.attributes['com.threecrickets.prudence.scheduler'] = scheduler

#
# Cache
#

component.context.attributes['com.threecrickets.prudence.cache'] = InProcessMemoryCache()
