<?php
//
// Stickstick Routing
//

global $router, $resourcesBaseURL;

$executable->container->include('defaults/application/routing/');

$router->capture(fixURL($resourcesBaseURL . '/data/note/{id}/'), '/data/note/');
?>