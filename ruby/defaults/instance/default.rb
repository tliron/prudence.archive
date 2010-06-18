#
# Prudence Component
#

require 'java'
import java.lang.System
import java.util.logging.LogManager
import com.threecrickets.scripturian.exception.DocumentNotFoundException

def execute_or_default(name, default=nil)
	begin
		$document.execute name
	rescue DocumentNotFoundException
		if !default
			default = 'defaults/' + name
		end
		$document.execute default
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

# Set Velocity to use log4j
$executable.manager.attributes['velocity.runtime.log.logsystem.class'] = 'org.apache.velocity.runtime.log.Log4JLogChute'
$executable.manager.attributes['velocity.runtime.log.logsystem.log4j.logger'] = 'velocity'

# Set spymemcached to use log4j
System::set_property 'net.spy.log.LoggerImpl', 'net.spy.memcached.compat.log.Log4JLogger'

#
# Configuration
#

# Hazelcast
System::set_property 'hazelcast.config', 'configuration/hazelcast.conf'

#
# Component
#

execute_or_default 'instance/component/'

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
