<?php
//
// Prudence Clients
//
// Copyright 2009-2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

global $component, $client_file, $client_http, $client_https;

import org.restlet.data.Protocol;

// Note: Quercus will not let us access
// Protocol::FILE or Protocol::HTTP directly.

// Required for use of Directory
$client_file = $component->clients->add(Protocol::valueOf('FILE'));

// Required for accessing external resources
$client_http = $component->clients->add(Protocol::valueOf('HTTP'));
$client_http->context->parameters->set('socketTimeout', 10000);
$client_https = $component->clients->add(Protocol::valueOf('HTTPS'));
$client_https->context->parameters->set('socketTimeout', 10000);
?>