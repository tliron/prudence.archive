<?php

// Helper to access the context attributes

function get_context_attribute($conversation, $name, $get_default_value) {
	$value = $conversation->resource->context->attributes->get($name);
	if(is_null($value )) {
		$value = $get_default_value();

		// Note: another thread might have changed our value in the meantime.
		// We'll make sure there is no duplication.

		$existing = $conversation->resource->context->attributes->putIfAbsent($name, $value);
		if(!is_null($existing)) {
			$value = $existing;
		}
	}
	return $value;
}

?>