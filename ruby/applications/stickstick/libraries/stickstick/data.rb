
import java.lang.System
import java.lang.Class
import java.util.concurrent.locks.ReentrantLock
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import org.restlet.Application
import com.threecrickets.prudence.util.MiniConnectionPoolManager

$connection_pool = nil
$connection_pool_lock = ReentrantLock.new

def get_data_source attributes
	data_source = nil
	if attributes['stickstick.backend'] == 'h2'
		data_source = org.h2.jdbcx.JdbcDataSource.new
	elsif attributes['stickstick.backend'] == 'mysql'
		data_source = com.mysql.jdbc.jdbc2.optional.MysqlDataSource.new
	end
	data_source.setURL get_url(attributes)
	data_source.user = attributes['stickstick.username']
	data_source.password = attributes['stickstick.password']
	return data_source
end

def get_url attributes
	url = 'jdbc:' + attributes['stickstick.backend'] + ':'
	if attributes['stickstick.host'] != ''
		if attributes['stickstick.backend'] == 'h2'
			url += 'tcp:'
		end
		url += '//' + attributes['stickstick.host'] + '/'
	end
	if attributes['stickstick.database'] != ''
		url += attributes['stickstick.database']
	end
	return url
end

def get_connection fresh=false
	attributes = Application.current.context.attributes

	$connection_pool_lock.lock
	begin
		if $connection_pool.nil? || fresh
			if $connection_pool.nil?
				$connection_pool = MiniConnectionPoolManager.new(get_data_source(attributes), 10)
			end
			
			# TODO: CREATE DATABASE IF NOT EXISTS
	
			connection = $connection_pool.connection
	
			if fresh
				statement = connection.create_statement
				begin
					statement.execute 'DROP TABLE board'
					statement.execute 'DROP TABLE note'
				rescue SQLException
					# Tables already gone
				ensure
					statement.close()
				end
			end
	
			statement = connection.create_statement
			begin
				statement.execute 'CREATE TABLE IF NOT EXISTS board (id VARCHAR(50) PRIMARY KEY, timestamp TIMESTAMP)'
				statement.execute 'CREATE TABLE IF NOT EXISTS note (id INT AUTO_INCREMENT PRIMARY KEY, board VARCHAR(50), x INT, y INT, size INT, content TEXT, timestamp TIMESTAMP)'
				statement.execute 'CREATE INDEX IF NOT EXISTS note_board_idx ON note (board)'
			rescue SQLException
				# Already exist
			ensure
				statement.close
			end
			
			begin
				add_board({'id' => 'Todo List'}, connection)
				add_board({'id' => 'Great Ideas'}, connection)
				add_board({'id' => 'Sandbox'}, connection)
			rescue SQLException
				# Boards already exist
			end
	
			connection.close
		end
		
		return $connection_pool.connection
	ensure
		$connection_pool_lock.unlock
	end
end

def get_boards connection
	boards = []

	statement = connection.create_statement
	begin
		rs = statement.execute_query 'SELECT id, timestamp FROM board'
		begin
			while rs.next do
				boards.push({
					'id' => rs.get_string(1),
					'timestamp' => rs.get_timestamp(2).time
				})
			end
		ensure
			rs.close
		end
	ensure
		statement.close()
	end

	return boards
end

def add_board board, connection
	statement = connection.prepare_statement 'INSERT INTO board (id, timestamp) VALUES (?, ?)'
	begin
		statement.set_string 1, board['id']
		statement.set_timestamp 2, Timestamp.new(System.current_time_millis)
		statement.execute
	ensure
		statement.close
	end
end

def update_board_timestamp note, connection, timestamp=nil
    # TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    # there is a chance that the board will not be set with the latest timestamp!

    if timestamp.nil?
		timestamp = note['timestamp']
	end

	statement = connection.prepare_statement 'UPDATE board SET timestamp=? WHERE id=?'
	begin
		statement.set_timestamp 1, Timestamp.new(timestamp)
		statement.set_string 2, note['board']
		statement.execute
	ensure
		statement.close
	end
end

def get_board_max_timestamp connection
	statement = connection.create_statement
	begin
		rs = statement.execute_query 'SELECT MAX(timestamp) FROM board'
		if rs.next
			return rs.get_timestamp(1).time
		end
	ensure
		statement.close
	end
	
	return nil
end

def get_note id, connection
	if !id
		return nil
	end

	statement = connection.prepare_statement 'SELECT board, x, y, size, content, timestamp FROM note WHERE id=?'
	begin
		statement.set_int 1, id
		rs = statement.execute_query
		begin
			if rs.next
				return {
					'id' => id,
					'board' => rs.get_string(1),
					'x' => rs.get_int(2),
					'y' => rs.get_int(3),
					'size' => rs.get_int(4),
					'content' => rs.get_string(5),
					'timestamp' => rs.get_timestamp(6).time
				}
			end
		ensure
			rs.close
		end
	ensure
		statement.close
	end

	return nil
end

def get_notes connection
	notes = Array.new

	statement = connection.create_statement
	begin
		rs = statement.execute_query 'SELECT id, board, x, y, size, content FROM note'
		begin
			while rs.next do
				notes << {
					'id' => rs.get_int(1),
					'board' => rs.get_string(2),
					'x' => rs.get_int(3),
					'y' => rs.get_int(4),
					'size' => rs.get_int(5),
					'content' => rs.get_string(6)
				}
			end
		ensure
			rs.close
		end
	ensure
		statement.close
	end

	return notes
end

def add_note note, connection
	note['timestamp'] = System.current_time_millis
	statement = connection.prepare_statement 'INSERT INTO note (board, x, y, size, content, timestamp) VALUES (?, ?, ?, ?, ?, ?)'
	begin
		statement.set_string 1, note['board']
		statement.set_int 2, note['x']
		statement.set_int 3, note['y']
		statement.set_int 4, note['size']
		statement.set_string 5, note['content']
		statement.set_timestamp 6, Timestamp.new(note['timestamp'])
		statement.execute
	ensure
		statement.close
	end
end

def update_note note, connection
    # TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    # there is a chance that the board will not be set with the latest timestamp!

	note['timestamp'] = System.current_time_millis
	statement = connection.prepare_statement 'UPDATE note SET board=?, x=?, y=?, size=?, content=?, timestamp=? WHERE id=?'
	begin
		statement.set_string 1, note['board']
		statement.set_int 2, note['x']
		statement.set_int 3, note['y']
		statement.set_int 4, note['size']
		statement.set_string 5, note['content']
		statement.set_timestamp 6, Timestamp.new(note['timestamp'])
		statement.set_int 7, note['id']
		statement.execute
	ensure
		statement.close
	end
end

def delete_note note, connection
	statement = connection.prepare_statement 'DELETE FROM note WHERE id=?'
	begin
		statement.set_int 1, note['id']
		statement.execute
	ensure
		statement.close
	end
end
