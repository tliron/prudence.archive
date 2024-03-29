#
# Prudence Clients
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

import org.restlet.data.Protocol

# Required for use of Directory
$client_file = $component.clients.add Protocol::FILE

# Required for accessing external resources
$client_http = $component.clients.add(Protocol::HTTP)
$client_http.connect_timeout = 10000
$client_http.context.parameters.set('socketTimeout', '10000')

# Required for accessing external resources
$client_https = $component.clients.add(Protocol::HTTPS)
$client_https.connect_timeout = 10000
$client_https.context.parameters.set('socketTimeout', '10000')
