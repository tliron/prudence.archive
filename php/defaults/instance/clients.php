<?php
//
// Prudence Clients
//
// Copyright 2009-2010 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

global $component;

import org.restlet.data.Protocol;

// Note: Quercus will not let us access
// Protocol::FILE or Protocol::HTTP directly.

// Required for use of Directory
$component->clients->add(Protocol::valueOf('FILE'));

// Required for accessing external resources
$component->clients->add(Protocol::valueOf('HTTP'));
$component->clients->add(Protocol::valueOf('HTTPS'));
?>