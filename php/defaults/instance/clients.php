<?php
//
// Prudence Clients
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