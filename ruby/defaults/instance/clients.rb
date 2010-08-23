#
# Prudence Clients
#

import org.restlet.data.Protocol

# Required for use of Directory
$component.clients.add Protocol::FILE

# Required for accessing external resources
$component.clients.add Protocol::HTTP
$component.clients.add Protocol::HTTPS
