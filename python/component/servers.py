#
# Prudence Servers
#
# Handles communication with clients.
#
# Often one server is enough, but Prudence supports multiple servers, so that you can
# handle requests coming through various ports and protocols, or to bind to different
# IP addresses (representing different network interfaces, VPNs, etc.) on your machine.
#
# A server can be set up to run behind another web server via a proxy.
# For Apache, this requires mod_proxy.
#
# Note that servers don't handle the actual routing. Your resources are instead attached
# to virtual hosts. See hosts.js for more information.
#

from org.restlet import Server
from org.restlet.data import Protocol

#
# Default HTTP server
#
# Binds to the machine's default IP address.
#

defaultServer = Server(Protocol.HTTP, 8080)
component.servers.add(defaultServer)

#
# HTTP server bound to a specific IP address
#
# This is an example of binding a server to an IP address representing one of
# several of the machine's network interfaces. In this case, let's pretend
# that it's the interface open to the Internet at large.
#

#worldServer = Server(Protocol.HTTP, '192.168.1.2', 80)
#component.servers.add(worldServer)

#
# Welcome
#

for i in range(len(component.servers)):
	server = component.servers[i]
	if server.address:
		sys.stdout.write('Starting server at %s port %s for ' % (server.address, server.port))
	else:
		sys.stdout.write('Starting server at port %s for ' % server.port)
	for j in range(len(server.protocols)):
		protocol = server.protocols[j]
		if j < len(server.protocols) - 1:
			sys.stdout.write(', ')
		sys.stdout.write(str(protocol))
	print '.'
