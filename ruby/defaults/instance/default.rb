#
# Prudence Instance
#
# Copyright 2009-2010 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

require 'java'
import java.lang.System
import java.util.logging.LogManager
import com.threecrickets.scripturian.exception.DocumentNotFoundException

def execute_or_default(name, default=nil)
	begin
		begin
			$document.execute name
			
			# Note: a bug in JRuby 1.5.1 causes the begin/rescue block to throw a
			# "assigning non-exception to $!" exception, which is why we are wrapping
			# this block in yet another begin/rescue block.
			
		rescue DocumentNotFoundException
			if default.nil?
				default = 'defaults/' + name
			end
			$document.execute default
		end
	rescue
		if default.nil?
			default = 'defaults/' + name
		end
		$document.execute default
	end
end

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

puts "Prudence #{$prudence_version}#{$prudence_revision} for #{$prudence_flavor}."

#
# Logging
#

# log4j: This is our actual logging engine
begin
	import org.apache.log4j.PropertyConfigurator
	System::set_property 'prudence.logs', $document.source.base_path.path + '/logs'
	PropertyConfigurator::configure $document.source.base_path.path + '/configuration/logging.conf'
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

puts 'Prudence is up!'
for server in $component.servers
	if server.address
		print "Listening on #{server.address} port #{server.port} for "
	else
		print "Listening on port #{server.port} for "
	end
	j = 0
	for protocol in server.protocols
		if j < server.protocols.size - 1
			print ', '
		end
		print protocol
		j += 1
	end
	puts '.'
end

#
# Scheduler
#

$scheduler.start

#
# Tasks
#

$fixed_executor = Executors::new_fixed_thread_pool(Runtime::runtime.available_processors * 2 + 1)
if $tasks.length > 0
	futures = []
	start_time = System::current_time_millis
	puts "Executing #{$tasks.length} startup tasks..."
	for task in $tasks
		futures << $fixed_executor.submit(task)
	end
	for future in futures
		future.get
	end
	puts "Finished all startup tasks in #{(System::current_time_millis - start_time) / 1000.0} seconds."
end
