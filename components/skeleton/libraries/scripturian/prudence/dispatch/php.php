<?php
global $resources;

$resources = array();

$document->executeOnce($application->globals['prudence.dispatch.php.library']);

function handle($conversation, $method) {
	global $resources;
	$id = $conversation->locals['prudence.id'];
	$resource = $resources[$id];
	if (is_null($resource)) {
		$conversation->statusCode = 404;
		return NULL;
	}
	if (!method_exists($resource, $method)) {
		$conversation->statusCode = 405;
		return NULL;
	}
	return $resource->{$method}($conversation);
}

function handle_init($conversation) {
	handle($conversation, 'handle_init');
}

function handle_get($conversation) {
	return handle($conversation, 'handle_get');
}

function handle_get_info($conversation) {
	return handle($conversation, 'handle_get_info');
}

function handle_post($conversation) {
	return handle($conversation, 'handle_post');
}

function handle_put($conversation) {
	return handle($conversation, 'handle_put');
}

function handle_delete($conversation) {
	return handle($conversation, 'handle_delete');
}
?>