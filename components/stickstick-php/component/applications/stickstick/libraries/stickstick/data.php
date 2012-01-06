<?php
import java.lang.System;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;

$connection_pool_lock = $application->globals['connection_pool_lock'];
if(is_null($connection_pool_lock)) {
	$connection_pool_lock = $application->getGlobal('connection_pool_lock', new ReentrantLock());
}

function get_data_source() {
	global $application;

	if($application->globals['stickstick.backend'] == 'h2') {
		import org.h2.jdbcx.JdbcDataSource;
		$data_source = new JdbcDataSource();
	} else if($application->globals['stickstick.backend'] == 'mysql') {
		import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
		$data_source = new MysqlDataSource();
	}
	$data_source->URL = get_url();
	$data_source->user = $application->globals['stickstick.username'];
	$data_source->password = $application->globals['stickstick.password'];
	return $data_source;
}

function get_url() {
	global $application;

	$url = 'jdbc:' . $application->globals['stickstick.backend'] . ':';
	if($application->globals['stickstick.host'] != '') {
		if($application->globals['stickstick.backend'] == 'h2') {
			$url = $url . 'tcp:';
		}
		$url = $url . '//' . $application->globals['stickstick.host'] . '/';
	}
	if($application->globals['stickstick.database'] != '') {
		$url = $url . $application->globals['stickstick.database'];
	}
	if($application->globals['stickstick.backend'] == 'h2') {
		$url = $url . ';MVCC=TRUE';
	}
	return $url;
}

function get_connection_pool() {
	$connection_pool = new GenericObjectPool(NULL, 10);
	new PoolableConnectionFactory(new DataSourceConnectionFactory(get_data_source()), $connection_pool, NULL, NULL, FALSE, TRUE);
	return new PoolingDataSource($connection_pool);
}

function get_connection($fresh=false) {
	global $connection_pool_lock, $application;
	
	$connection_pool_lock->lock();
	$connection_pool = $application->globals['connection_pool'];
	try {
		if(is_null($connection_pool) || $fresh) {
			if(is_null($connection_pool)) {
				$connection_pool = $application->getGlobal('connection_pool', get_connection_pool());
			}
			
			// TODO: CREATE DATABASE IF NOT EXISTS
	
			$connection = $connection_pool->connection;
	
			if($fresh) {
				//print "fresh\n";
				$statement = $connection->createStatement();
				try {
					$statement->execute('DROP TABLE board');
					$statement->execute('DROP TABLE note');
				}
				catch(Exception $x) {
					// Tables already gone
				}
				$statement->close();
			}
	
			$statement = $connection->createStatement();
			try {
				$statement->execute('CREATE TABLE IF NOT EXISTS board (id VARCHAR(50) PRIMARY KEY, timestamp TIMESTAMP)');
				$statement->execute('CREATE TABLE IF NOT EXISTS note (id INT AUTO_INCREMENT PRIMARY KEY, board VARCHAR(50), x INT, y INT, size INT, content TEXT, timestamp TIMESTAMP)');
				$statement->execute('CREATE INDEX IF NOT EXISTS note_board_idx ON note (board)');
			}
			catch(Exception $x) {
				// Already exist
			}
			$statement->close();
			
			try {
				add_board(array('id' => 'Todo List'), $connection);
				add_board(array('id' => 'Great Ideas'), $connection);
				add_board(array('id' => 'Sandbox'), $connection);
				add_note(array('board' => 'Todo List', 'x' => 100, 'y' => 100, 'size' => 0, 'content' => 'Test!'), $connection);
			}
			catch(Exception $x) {
				// Boards already exist
				//print $x."\n";
			}
	
			$connection->close();
		}
		
		$r = $connection_pool->connection; 
		$connection_pool_lock->unlock();
		return $r;
	}
	catch(Exception $x) {
		$connection_pool_lock->unlock();
		throw $x;
	}
	$connection_pool_lock->unlock();
}

function get_boards($connection) {
	$boards = array();

	$statement = $connection->createStatement();
	try {
		$rs = $statement->executeQuery('SELECT id, timestamp FROM board');
		try {
			while($rs->next()) {
				$boards[] = array(
					'id' => $rs->getString(1),
					'timestamp' => $rs->getTimestamp(2)->time
				);
			}
		}
		catch(Exception $x) {
			$rs->close();
			$statement->close();
			throw $x;
		}
		$rs->close();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();

	return $boards;
}

function add_board($board, $connection) {
	$statement = $connection->prepareStatement('INSERT INTO board (id, timestamp) VALUES (?, ?)');
	try {
		$statement->setString(1, $board['id']);
		$statement->setObject(2, new Timestamp(System::currentTimeMillis()));
		$statement->execute();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();
}

function update_board_timestamp($note, $connection, $timestamp=NULL) {
    // TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    // there is a chance that the board will not be set with the latest timestamp!

    if(is_null($timestamp)) {
		$timestamp = $note['timestamp'];
	}

	$statement = $connection->prepareStatement('UPDATE board SET timestamp=? WHERE id=?');
	try {
		$statement->setObject(1, new Timestamp($timestamp));
		$statement->setString(2, $note['board']);
		$statement->execute();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();
}

function get_board_max_timestamp($connection) {
	$statement = $connection->createStatement();
	try {
		$rs = $statement->executeQuery('SELECT MAX(timestamp) FROM BOARD');
		if($rs->next()) {
			$r = $rs->getTimestamp(1)->time;
			$statement->close();
			return $r;
		}
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();
	
	return NULL;
}

function get_note($id, $connection) {
	if(!$id) {
		return NULL;
	}
	
	$statement = $connection->prepareStatement('SELECT board, x, y, size, content, timestamp FROM note WHERE id=?');
	try {
		$statement->setInt(1, $id);
		$rs = $statement->executeQuery();
		try {
			if($rs->next()) {
				$r = array(
					'id' => $id,
					'board' => $rs->getString(1),
					'x' => $rs->getInt(2),
					'y' => $rs->getInt(3),
					'size' => $rs->getInt(4),
					'content' => $rs->getString(5),
					'timestamp' => $rs->getTimestamp(6)->time
				);
				$rs->close();
				$statement->close();
				return $r;
			}
		}
		catch(Exception $x) {
			$rs->close();
			$statement->close();
			throw $x;
		}
		$rs->close();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();
	
	return NULL;
}

function get_notes($connection) {
	$notes = array();

	$statement = $connection->createStatement();
	try {
		$rs = $statement->executeQuery('SELECT id, board, x, y, size, content FROM note');
		try {
			while($rs->next()) {
				$notes[] = array(
					'id' => $rs->getInt(1),
					'board' => $rs->getString(2),
					'x' => $rs->getInt(3),
					'y' => $rs->getInt(4),
					'size' => $rs->getInt(5),
					'content' => $rs->getString(6)
				);
			}
		}
		catch(Exception $x) {
			$rs->close();
			$statement->close();
			throw $x;
		}
		$rs->close();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();

	return $notes;
}

function add_note($note, $connection) {
	$note['timestamp'] = System::currentTimeMillis();
	$statement = $connection->prepareStatement('INSERT INTO note (board, x, y, size, content, timestamp) VALUES (?, ?, ?, ?, ?, ?)');
	try {
		$statement->setString(1, $note['board']);
		$statement->setInt(2, $note['x']);
		$statement->setInt(3, $note['y']);
		$statement->setInt(4, $note['size']);
		$statement->setString(5, $note['content']);
		$statement->setObject(6, new Timestamp($note['timestamp']));
		$statement->execute();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();
}

function update_note($note, $connection) {
    // TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    // there is a chance that the board will not be set with the latest timestamp!

	$note['timestamp'] = System::currentTimeMillis();
	$statement = $connection->prepareStatement('UPDATE note SET board=?, x=?, y=?, size=?, content=?, timestamp=? WHERE id=?');
	try {
		$statement->setString(1, $note['board']);
		$statement->setInt(2, $note['x']);
		$statement->setInt(3, $note['y']);
		$statement->setInt(4, $note['size']);
		$statement->setString(5, $note['content']);
		$statement->setObject(6, new Timestamp($note['timestamp']));
		$statement->setInt(7, $note['id']);
		$statement->execute();
	}
	catch(Exception $x) {
		//print $x;
		$statement->close();
		throw $x;
	}
	$statement->close();
}

function delete_note($note, $connection) {
	$statement = $connection->prepareStatement('DELETE FROM note WHERE id=?');
	try {
		$statement->setInt(1, $note['id']);
		$statement->execute();
	}
	catch(Exception $x) {
		$statement->close();
		throw $x;
	}
	$statement->close();
}
?>