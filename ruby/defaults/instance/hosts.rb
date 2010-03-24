#
# Prudence Hosts
#
# Prudence supports virtual hosting, allowing you to serve different applications
# or otherwise route differently per domain name, protocol, port, etc. This
# feature lets you run multiple sites from the same Prudence installation.
#
# Note that virtual hosts are only indirectly related to Prudence's servers.
# See servers.js for more information.
#

import org.restlet.routing.VirtualHost

#
# All
#
# Our "all" host will accept all incoming requests.
#

$all_host = VirtualHost.new $component.context
$all_host.name = 'all hosts'

$component.hosts.add $all_host

#
# mysite.org
#
# This is an example of a virtual host which only accepts requests to
# a specific set of domains.
#

$mysite_host = VirtualHost.new $component.context
$mysite_host.name = 'mysite.org'
$mysite_host.host_scheme = 'http'
$mysite_host.host_domain = 'mysite.org|www.mysite.org'
$mysite_host.host_port = '80'

$component.hosts.add $mysite_host

#
# Default Host
#
# Applications by default will attach only to this host, though they can
# choose to attach to any hosts defined here. See the application's
# routing.js.
#

$component.default_host = $all_host
