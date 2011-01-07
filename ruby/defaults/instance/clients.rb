#
# Prudence Clients
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

import org.restlet.data.Protocol

# Required for use of Directory
$component.clients.add Protocol::FILE

# Required for accessing external resources
$component.clients.add Protocol::HTTP
$component.clients.add Protocol::HTTPS
