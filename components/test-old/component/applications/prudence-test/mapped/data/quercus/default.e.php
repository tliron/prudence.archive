<?php

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.restlet.ext.json.JsonRepresentation;

// Include the context library
require 'quercus/context.php';

// State
//
// These make sure that our state is properly stored in the context,
// so that we always use the same state, even if this script is recompiled.
//
// PHP normally passes arrays by value. In our case, we want to make sure
// to pass our state by reference, hence the "&". 

function new_lock() {
	return new ReentrantReadWriteLock();
}

function get_stack_lock($conversation) {
	return get_context_attribute($conversation, 'quercus.stateLock', 'new_lock');
}

function new_state() {
	return array('name' => 'Coraline', 'media' => 'Film', 'rating' => 'A+', 'characters' => array('Coraline', 'Wybie', 'Mom', 'Dad'));
}

function &get_state($conversation) {
	return get_context_attribute($conversation, 'quercus.state', 'new_state');
}

function set_state($conversation, $value) {
	$conversation->resource->context->attributes->put('quercus.state', $value); 
}

function handle_init($conversation) {
    $conversation->addMediaTypeByName('application/json');
    $conversation->addMediaTypeByName('text/plain');
}

function handle_get($conversation) {
	$state_lock = get_stack_lock($conversation);
	$state =& get_state($conversation);

	$state_lock->readLock()->lock();
	try {
		$r = json_encode($state);
	}
	catch(Exception $x) {
		$state_lock->readLock()->unlock();
		throw $x;
	}
	$state_lock->readLock()->unlock();

	if($r == '[]') {
		// json_encode and PHP don't distinguish between an empty associative
		// array and an empty numerical array
		$r = '{}';
	}
		
	// Return a representation appropriate for the requested media type
	// of the possible options we created in handle_init()

	if($conversation->mediaTypeName == 'application/json') {
		$r = new JsonRepresentation($r);
	}

	return $r;
}

function handle_post($conversation) {
	// Note: we must call ->getText() explicitly; ->text will not work

	$update = json_decode($conversation->entity->getText(), true);
	$state_lock = get_stack_lock($conversation);
	$state =& get_state($conversation);
	
	$state_lock->writeLock()->lock();
	try {
		foreach($update as $key => $value) {
			print $key . '=' . $value . "\n";
			$state[$key] = $value;
		}
	}
	catch(Exception $x) {
		$state_lock->writeLock()->unlock();
		throw $x;
	}
	$state_lock->writeLock()->unlock();
		
	return handle_get($conversation);
}

function handle_put($conversation) {
	// Note: we must call ->getText() explicitly; ->text will not work
	
	$update = json_decode($conversation->entity->getText(), true);
	set_state($conversation, $update);

	return handle_get($conversation);
}

function handle_delete($conversation) {
	set_state($conversation, array());

	return NULL;
}
?>