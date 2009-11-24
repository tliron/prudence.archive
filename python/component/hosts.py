#
# Prudence Hosts
#
# Prudence supports virtual hosting, allowing you to serve different applications
# or otherwise route differently per domain name, protocol, port, etc. This
# feature lets you run multiple sites from the same Prudence installation.
#
# Note that virtual hosts are only indirectly related to Prudence's servers.
# See servers.py for more information.
#

from org.restlet.routing import VirtualHost

#
# Wildcard
#
# Our "wildcard" host will accept all incoming requests.
#

wildcardHost = VirtualHost(component.context)
wildcardHost.name = 'wildcard'

component.hosts.add(wildcardHost)

#
# mysite.org
#
# This is an example of a virtual host which only accepts requests to
# a specific set of domains.
#

mysiteHost = VirtualHost(component.context)
mysiteHost.name = 'mysite.org'
mysiteHost.hostScheme = 'http'
mysiteHost.hostDomain = 'mysite.org|www.mysite.org'
mysiteHost.hostPort = '80'

component.hosts.add(mysiteHost)

#
# Default Host
#
# Applications by default will attach only to this host, though they can
# choose to attach to any hosts defined here. See the application's
# routing.js.
#

component.defaultHost = wildcardHost

#
# Welcome
#

sys.stdout.write('Available virtual hosts: ')
for i in range(len(component.hosts)):
	host = component.hosts[i]
	sys.stdout.write('"%s"' % host.name)
	if i < len(component.hosts) - 1:
		sys.stdout.write(', ')
print '.'
