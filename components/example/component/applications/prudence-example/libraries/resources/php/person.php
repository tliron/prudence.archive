<?php

class Person {
	function handle_init($conversation) {
		$conversation->addMediaTypeByName('text/html');
		$conversation->addMediaTypeByName('text/plain');
	}

	function handle_get($conversation) {
		$id = $conversation->locals['id'];
		return "I am person {$id}, formatted as \"{$conversation->mediaTypeName}\", encased in PHP";
	}
}

?>