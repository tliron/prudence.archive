<?php
//
// This script implements and handles a REST resource. Simply put, it is a state,
// addressed by a URL, that responds to verbs. Verbs represent logical operations
// on the state, such as create, read, update and delete (CRUD). They are primitive
// communications, which include very minimal session and no transaction state. As such,
// they are very straightforward to implement, and can lead to very scalable
// applications. 
//
// The exact URL of this resource depends on its its filename and/or its location in
// your directory structure. See your settings.php for more information.
//

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.restlet.ext.json.JsonRepresentation;

// Include the context library
$prudence->execute('../libraries/quercus/context/');

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

function &get_stack_lock($conversation) {
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

// This function is called when the resource is initialized. We will use it to set
// general characteristics for the resource.

function handle_init($conversation) {
	// The order in which we add the variants is their order of preference.
	// Note that clients often include a wildcard (such as "*/*") in the
	// "Accept" attribute of their request header, specifying that any media type
	// will do, in which case the first one we add will be used.
	
    $conversation->addMediaTypeByName('application/json');
    $conversation->addMediaTypeByName('text/plain');
}

// This function is called for the GET verb, which is expected to behave as a
// logical "read" of the resource's state.
//
// The expectation is that it return one representation, out of possibly many, of the
// resource's state. Returned values can be of any explicit sub-class of
// org.restlet.resource.Representation. Other types will be automatically converted to
// string representation using the client's requested media type and character set.
// These, and the language of the representation (defaulting to NULL), can be read and
// changed via $conversation->mediaType, $conversation->characterSet, and
// $conversation->language.
//
// Additionally, you can use $conversation->variant to interrogate the client's provided
// list of supported languages and encoding.

function handle_get($conversation) {

	$state_lock = get_stack_lock($conversation);
	$state = &get_state($conversation);

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

// This function is called for the POST verb, which is expected to behave as a
// logical "update" of the resource's state.
//
// The expectation is that $conversation->entity represents an update to the state,
// that will affect future calls to handle_get(). As such, it may be possible
// to accept logically partial representations of the state.
//
// You may optionally return a representation, in the same way as handle_get().
// Because PHP functions return the last statement's value by default,
// you must explicitly return a NULL if you do not want to return a representation
// to the client.

function handle_post($conversation) {

	$update = json_decode($conversation->entity->text, true);
	$state_lock = get_stack_lock($conversation);
	$state = &get_state($conversation);
	
	$state_lock->writeLock()->lock();
	try {
		foreach($update as $key => $value) {
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

// This function is called for the PUT verb, which is expected to behave as a
// logical "create" of the resource's state.
//
// The expectation is that $conversation->entity represents an entirely new state,
// that will affect future calls to handle_get(). Unlike handle_post(),
// it is expected that the representation be logically complete.
//
// You may optionally return a representation, in the same way as handle_get().
// Because PHP functions return the last statement's value by default,
// you must explicitly return a NULL if you do not want to return a representation
// to the client.

function handle_put($conversation) {

	$update = json_decode($conversation->entity->text, true);
	set_state($conversation, $update);

	return handle_get($conversation);
}

// This function is called for the DELETE verb, which is expected to behave as a
// logical "delete" of the resource's state.
//
// The expectation is that subsequent calls to handle_get() will fail. As such,
// it doesn't make sense to return a representation, and any returned value will
// ignored. Still, it's a good idea to return NULL to avoid any passing of value.

function handle_delete($conversation) {

	set_state($conversation, array());

	return NULL;
}
?>