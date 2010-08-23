#
# Prudence Clients
#

from org.restlet.data import Protocol

# Required for use of Directory
component.clients.add(Protocol.FILE)

# Required for accessing external resources
component.clients.add(Protocol.HTTP)
component.clients.add(Protocol.HTTPS)
