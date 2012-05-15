#
# Prudence Hosts
#
# Copyright 2009-2011 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.gnu.org/copyleft/lesser.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

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
# All
#
# Our "all" host will accept all incoming requests.
#

all_host = VirtualHost(component.context)
all_host.name = 'all hosts'

component.hosts.add(all_host)

#
# mysite.org
#
# This is an example of a virtual host which only accepts requests to
# a specific set of domains.
#

mysite_host = VirtualHost(component.context)
mysite_host.name = 'mysite.org'
mysite_host.hostScheme = 'http'
mysite_host.hostDomain = 'mysite.org|www.mysite.org'
mysite_host.hostPort = '80'

component.hosts.add(mysite_host)

#
# Default Host
#
# Applications by default will attach only to this host, though they can
# choose to attach to any hosts defined here. See the application's
# routing.py.
#

component.defaultHost = all_host
