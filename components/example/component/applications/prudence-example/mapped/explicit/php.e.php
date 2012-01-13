<?php

import java.util.concurrent.locks.ReentrantReadWriteLock;

// Include the context library
require 'data/php.php';

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

function get_stack_lock() {
	return get_global('php.stateLock', 'new_lock');
}

function new_state() {
	return array('name' => 'Coraline', 'media' => 'Film', 'rating' => 'A+', 'characters' => array('Coraline', 'Wybie', 'Mom', 'Dad'));
}

function &get_state() {
	return get_global('php.state', 'new_state');
}

function set_state($value) {
	global $application;
	$application->globals['php.state'] = $value; 
}

function handle_init($conversation) {
    $conversation->addMediaTypeByName('application/json');
    $conversation->addMediaTypeByName('text/plain');
}

function handle_get($conversation) {
	$state_lock = get_stack_lock();
	$state =& get_state();

	$state_lock->readLock()->lock();
	try {
		// Note: json_encode will not work if the array was created in another environment,
		// so we are creating a clone in this environment
		$r = json_encode(clone $state);
	}
	catch (Exception $x) {
		$state_lock->readLock()->unlock();
		throw $x;
	}
	$state_lock->readLock()->unlock();

	if ($r == '[]') {
		// json_encode and PHP don't distinguish between an empty associative
		// array and an empty numerical array
		$r = '{}';
	}
		
	return $r;
}

function handle_post($conversation) {
	// Note: we must call ->getText() explicitly; ->text will not work

	$update = json_decode($conversation->entity->getText(), true);
	$state_lock = get_stack_lock();
	$state =& get_state();
	
	$state_lock->writeLock()->lock();
	try {
		$new_state = array_merge($state, $update);
	}
	catch (Exception $x) {
		$state_lock->writeLock()->unlock();
		throw $x;
	}
	$state_lock->writeLock()->unlock();

	set_state($new_state);
	
	return handle_get($conversation);
}

function handle_put($conversation) {
	// Note: we must call ->getText() explicitly; ->text will not work
	
	$update = json_decode($conversation->entity->getText(), true);
	set_state($update);

	return handle_get($conversation);
}

function handle_delete($conversation) {
	set_state(array());

	return NULL;
}
?>