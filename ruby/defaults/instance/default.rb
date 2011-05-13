#
# Prudence Instance
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
import java.lang.System
import java.util.logging.LogManager
import java.util.concurrent.Executors
import java.util.concurrent.CopyOnWriteArrayList
import com.threecrickets.scripturian.document.DocumentFileSource
import com.threecrickets.scripturian.exception.DocumentNotFoundException
import com.threecrickets.prudence.service.ApplicationService

#
# Common
#

$common_libraries_document_source = DocumentFileSource.new 'common/libraries/', java.io.File.new($document.source.base_path, 'common/libraries/'), 'default', 'rb', 5000
$common_fragments_document_source = DocumentFileSource.new 'common/web/fragments/', java.io.File.new($document.source.base_path, 'common/web/fragments/'), 'index', 'rb', 5000

$common_tasks_document_sources = CopyOnWriteArrayList.new
$common_tasks_document_sources << DocumentFileSource.new('common/tasks/', java.io.File.new($document.source.base_path, 'common/tasks/'), 'default', 'rb', 5000)
$common_handlers_document_sources = CopyOnWriteArrayList.new
$common_handlers_document_sources << DocumentFileSource.new('common/handlers/', java.io.File.new($document.source.base_path, 'common/handlers/'), 'default', 'rb', 5000)

$document.library_sources.add $common_libraries_document_source

#
# Utilities
#

def execute_or_default(name, default=nil)
	begin
		# Note: a bug in JRuby 1.6.0 causes exceptions not to be caught if we call $document.execute normally
		# from within compiled Ruby code

		$document.java_send :execute, [java.lang.String], name
	rescue DocumentNotFoundException
		if default.nil?
			default = '/defaults/' + name
		end
		$document.execute default
	end
end

#
# Version
#

$prudence_version = '1.1'
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
# Predefined Shared Globals
#
# These will be available to your code via application.sharedGlobals.
#

for key in $predefined_shared_globals.keys
	$component.context.attributes[key] = $predefined_shared_globals[key]
end

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
	server.protocols.each_with_index do |protocol, i|
		if i < server.protocols.size - 1
			print ', '
		end
		print protocol
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
	$futures = []
	start_time = System::current_time_millis
	puts "Executing #{$tasks.length} startup tasks..."
	for task in $tasks
		$futures << $fixed_executor.submit(task)
	end
	for future in $futures
		begin
			future.get
		rescue
		end
	end
	puts "Finished all startup tasks in #{(System::current_time_millis - start_time) / 1000.0} seconds."
end

for application in $applications
	application_service = ApplicationService.new application
	application_service.task nil, '/startup/', nil, 'initial', 0, 0, false
end
