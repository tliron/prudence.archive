<?php
require 'stickstick/data.php';

import java.lang.System;

function merge($key, $a, $b) {
	if(!in_array($key, $a)) {
		return $b[$key];
	}
	else {
		return $a[$key];
	}
}

function get_id($conversation) {
    return intval($conversation->locals['id']);

    //return intval($_GET['id']);
}

function handle_init($conversation) {
    $conversation->addMediaTypeByName('text/plain');
    $conversation->addMediaTypeByName('application/json');
}

function handle_get($conversation) {
	$id = get_id($conversation);

    $note = NULL;
    $connection = get_connection();
    print $connection;
    try {
        $note = get_note($id, $connection);
        if(is_null($note)) {
    		$connection->close();
        	return 404;
        }
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
    $connection->close();

    $conversation->modificationTimestamp = $note['timestamp'];
    unset($note['timestamp']);
    return json_encode($note);
}

function handle_get_info($conversation) {
	$id = get_id($conversation);

    $note = NULL;
    $connection = get_connection();
    try {
        $note = get_note($id, $connection);
        if(is_null($note)) {
    		$connection->close();
        	return NULL;
        }
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
    $connection->close();
   
    return $note['timestamp'];
}

function handle_post($conversation) {
	$id = get_id($conversation);

    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    $text = $conversation->entity->text;
    $note = json_decode($text, true);
    
    $connection = get_connection();
    try {
        $existing = get_note($id, $connection);
        if(is_null($existing)) {
    		$connection->close();
        	return 404;
        }
        $note = array_merge($existing, $note);
        update_note($note, $connection);
        update_board_timestamp($note, $connection);
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
    $connection->close();

    $conversation->modificationTimestamp = $note['timestamp'];
    unset($note['timestamp']);
    return json_encode($note);
}

function handle_delete($conversation) {
	$id = get_id($conversation);

    $connection = get_connection();
    try {
        $note = get_note($id, $connection);
        if(is_null($note)) {
    		$connection->close();
        	return 404;
        }
        delete_note($note, $connection);
        update_board_timestamp($note, $connection, System::currentTimeMillis());
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
    $connection->close();
    
    return NULL;
}
?>