#
# Prudence Component
#

require 'java'
import java.lang.System
import java.lang.Runtime
import java.io.FileNotFoundException
import java.util.logging.LogManager
import java.util.concurrent.Executors
import org.restlet.Component
import com.threecrickets.prudence.util.DelegatedStatusService

def execute_or_default(name, default=nil)
	begin
		$executable.container.execute name
	rescue FileNotFoundException
		if !default
			default = 'defaults/' + name
		end
		$executable.container.execute default
	end
end

$tasks = []

#
# Version
#

$prudence_version = '1.0'
$prudence_revision = '-%REVISION%'
if $prudence_revision.length == 1
	$prudence_revision = ''
end
$prudence_flavor = 'Ruby'

#
# Welcome
#

puts 'Prudence ' + $prudence_version + $prudence_revision + ' for ' + $prudence_flavor + '.'

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

# log4j: This is our actual logging engine
begin
	import org.apache.log4j.PropertyConfigurator
	PropertyConfigurator.configure 'configuration/logging.conf'
rescue
end

# JULI: Remove any pre-existing configuration
LogManager::log_manager.reset

# JULI: Bridge to SLF4J, which will use log4j as its engine 
begin
	import org.slf4j.bridge.SLF4JBridgeHandler
	SLF4JBridgeHandler::install
rescue
end

# Set Restlet to use SLF4J, which will use log4j as its engine
System::set_property 'org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade'

# Velocity logging
System::set_property 'com.sun.script.velocity.properties', 'configuration/velocity.conf'

# Web requests
$component.log_service.logger_name = 'web-requests'

#
# StatusService
#

$component.status_service = DelegatedStatusService.new

#
# Executor
#

$executor = Executors.new_fixed_thread_pool Runtime::runtime.available_processors
$component.context.attributes['prudence.executor'] = $executor

#
# Clients
#

execute_or_default 'instance/clients/'

#
# Routing
#

execute_or_default 'instance/routing/'

#
# Servers
#

execute_or_default 'instance/servers/'

#
# Start
#

$component.start

#
# Tasks
#

if $tasks.length > 0
	futures = []
	start_time = System.current_time_millis
	print 'Executing ', $tasks.length, ' tasks...'
	for task in $tasks
		futures << $executor.submit(task)
	end
	for future in futures
		future.get
	end
	print 'Finished tasks in ', (System.current_time_millis - start_time) / 1000.0, ' seconds.'
end
