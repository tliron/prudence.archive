<?php
require 'stickstick/data.php';

function handle_init($conversation) {
    $conversation->addMediaTypeByName('text/plain');
    $conversation->addMediaTypeByName('application/json');
}

function handle_get($conversation) {
    $fresh = $_GET['fresh'] == 'true';
    
    $max_timestamp = NULL;
    $board_list = array();

    $connection = get_connection($fresh);
    try {
	    $boards = get_boards($connection);
	    if(!is_null($boards)) {
		    foreach($boards as $board) {
		    	$board_list[] = $board['id'];
		        $timestamp = $board['timestamp'];
		        if(is_null($max_timestamp) || ($timestamp > $max_timestamp)) {
		            $max_timestamp = $timestamp;
		        }
		    }
	    }
	    else {
    		$connection->close();
	    	return null;
	    }
	
	    $notes = get_notes($connection);
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
    $connection->close();

    if(!is_null($max_timestamp)) {
        $conversation->modificationTimestamp = $max_timestamp;
    }
    return json_encode(array('boards' => $board_list, 'notes' => $notes));
}

function handle_get_info($conversation) {
    $connection = get_connection();
    try {
    	$r = get_board_max_timestamp($connection);
    	$connection->close();
    	return $r;
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
}

function handle_put($conversation) {
    // Note: You can only "consume" the entity once, so if we want it
    // as text, and want to refer to it more than once, we should keep
    // a reference to that text.
    
    $text = $conversation->entity->getText();
    $note = json_decode($text, true);
    
    $connection = get_connection();
    try {
    	add_note($note, $connection);
        update_board_timestamp($note, $connection);
    }
    catch(Exception $x) {
    	$connection->close();
    	throw $x;
    }
    $connection->close();
    
    return handle_get($conversation);
}
?>