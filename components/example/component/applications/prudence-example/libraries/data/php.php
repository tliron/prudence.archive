<?php

// Helper to access the application globals

// Be sure to accept the return value by reference if you want to modify it!

function &get_global($name, $get_default_value) {
	global $application;
	$value =& $application->globals[$name];
	if (is_null($value)) {
		$value = $get_default_value();

		// Note: another thread might have changed our value in the meantime.
		// We'll make sure there is no duplication.

		$existing =& $application->globals->putIfAbsent($name, $value);
		if (!is_null($existing)) {
			$value =& $existing;
		}
	}
	return $value;
}

?>