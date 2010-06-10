#
# Prudence Component
#

require 'java'
import java.lang.Runtime
import java.util.concurrent.Executors
import org.restlet.Component
import com.threecrickets.prudence.util.DelegatedStatusService
import com.threecrickets.prudence.cache.InProcessMemoryCache

#
# Component
#

$component = Component.new

$component.context.attributes['prudence.version'] = $prudence_version
$component.context.attributes['prudence.revision'] = $prudence_revision
$component.context.attributes['prudence.flavor'] = $prudence_flavor

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

$executor = Executors.new_fixed_thread_pool(Runtime::runtime.available_processors * 2 + 1)
$component.context.attributes['prudence.executor'] = $executor

#
# Cache
#

$component.context.attributes['com.threecrickets.prudence.cache'] = InProcessMemoryCache.new
