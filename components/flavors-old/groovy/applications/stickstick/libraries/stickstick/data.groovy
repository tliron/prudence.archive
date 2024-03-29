
import java.lang.System
import java.lang.Class
import java.util.concurrent.locks.ReentrantLock
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import org.restlet.Application
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.commons.dbcp.DataSourceConnectionFactory
import org.apache.commons.dbcp.PoolableConnectionFactory
import org.apache.commons.dbcp.PoolingDataSource

def connectionPoolLock = application.globals['connectionPoolLock']
if(connectionPoolLock == null) {
	connectionPoolLock = application.getGlobal('connectionPoolLock', new ReentrantLock())
}

getDataSource = {
	def dataSource
	if(application.globals['stickstick.backend'] == 'h2') {
		dataSource = new org.h2.jdbcx.JdbcDataSource()
	} else if(application.globals['stickstick.backend'] == 'mysql') {
		dataSource = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource()
	}
	dataSource.URL = getUrl()
	dataSource.user = application.globals['stickstick.username']
	dataSource.password = application.globals['stickstick.password']
	return dataSource
}

getUrl = {
	def url = 'jdbc:' + application.globals['stickstick.backend'] + ':'
	if(application.globals['stickstick.host'] != '') {
		if(application.globals['stickstick.backend'] == 'h2') {
			url += 'tcp:'
		}
		url += '//' + application.globals['stickstick.host'] + '/'
	}
	if(application.globals['stickstick.database'] != '') {
		url += application.globals['stickstick.database']
	}
	if(application.globals['stickstick.backend'] == 'h2') {
		url += ';MVCC=TRUE'
	}
	return url
}

getConnectionPool = {
	connectionPool = new GenericObjectPool(null, 10)
	new PoolableConnectionFactory(new DataSourceConnectionFactory(getDataSource()), connectionPool, null, null, false, true)
	return new PoolingDataSource(connectionPool)
}

getConnection = { fresh=false ->
	connectionPoolLock.lock()
	connectionPool = application.globals['connectionPool']
	try {
		if(connectionPool == null || fresh) {
			if(connectionPool == null) {
				connectionPool = application.getGlobal('connectionPool', getConnectionPool())
			}
			
			// TODO: CREATE DATABASE IF NOT EXISTS
	
			def connection = connectionPool.connection
	
			if(fresh) {
				def statement = connection.createStatement()
				try {
					statement.execute('DROP TABLE board')
					statement.execute('DROP TABLE note')
				}
				catch(SQLException) {
					// Tables already gone
				}
				finally {
					statement.close()
				}
			}
	
			def statement = connection.createStatement()
			try {
				statement.execute('CREATE TABLE IF NOT EXISTS board (id VARCHAR(50) PRIMARY KEY, timestamp TIMESTAMP)')
				statement.execute('CREATE TABLE IF NOT EXISTS note (id INT AUTO_INCREMENT PRIMARY KEY, board VARCHAR(50), x INT, y INT, size INT, content TEXT, timestamp TIMESTAMP)')
				statement.execute('CREATE INDEX IF NOT EXISTS note_board_idx ON note (board)')
			}
			catch(SQLException) {
				// Already exist
			}
			finally {
				statement.close()
			}
			
			try {
				addBoard([id: 'Todo List'], connection)
				addBoard([id: 'Great Ideas'], connection)
				addBoard([id: 'Sandbox'], connection)
			}
			catch(SQLException) {
				// Boards already exist
			}
	
			connection.close()
		}
		
		return connectionPool.connection
	}
	finally {
		connectionPoolLock.unlock()
	}
}

getBoards = { connection ->
	def boards = []

	def statement = connection.createStatement()
	try {
		def rs = statement.executeQuery('SELECT id, timestamp FROM board')
		try {
			while(rs.next()) {
				boards.push([
					id: rs.getString(1),
					timestamp: rs.getTimestamp(2).time
				])
			}
		}
		finally {
			rs.close()
		}
	}
	finally {
		statement.close()
	}

	return boards
}

addBoard = { board, connection ->
	def statement = connection.prepareStatement('INSERT INTO board (id, timestamp) VALUES (?, ?)')
	try {
		statement.setString(1, board.id)
		statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()))
		statement.execute()
	}
	finally {
		statement.close()
	}
}

updateBoardTimestamp = { note, connection, timestamp=null ->
    // TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    // there is a chance that the board will not be set with the latest timestamp!

    if(timestamp == null) {
		timestamp = note.timestamp
	}

	def statement = connection.prepareStatement('UPDATE board SET timestamp=? WHERE id=?')
	try {
		statement.setTimestamp(1, new Timestamp(timestamp))
		statement.setString(2, note.board)
		statement.execute()
	}
	finally {
		statement.close()
	}
}

getBoardMaxTimestamp = { connection ->
	def statement = connection.createStatement()
	try {
		def rs = statement.executeQuery('SELECT MAX(timestamp) FROM BOARD')
		if(rs.next()) {
			return rs.getTimestamp(1).time
		}
	}
	finally {
		statement.close()
	}
	
	return null
}

getNote = { id, connection ->
	if(!id) {
		return null
	}

	def statement = connection.prepareStatement('SELECT board, x, y, size, content, timestamp FROM note WHERE id=?')
	try {
		statement.setInt(1, id)
		def rs = statement.executeQuery()
		try {
			if(rs.next()) {
				return [
					id: id,
					board: rs.getString(1),
					x: rs.getInt(2),
					y: rs.getInt(3),
					size: rs.getInt(4),
					content: rs.getString(5),
					timestamp: rs.getTimestamp(6).time
				]
			}
		}
		finally {
			rs.close()
		}
	}
	finally {
		statement.close()
	}

	return null
}

getNotes = { connection ->
	def notes = []

	def statement = connection.createStatement()
	try {
		def rs = statement.executeQuery('SELECT id, board, x, y, size, content FROM note')
		try {
			while(rs.next()) {
				notes.add([
					id: rs.getInt(1),
					board: rs.getString(2),
					x: rs.getInt(3),
					y: rs.getInt(4),
					size: rs.getInt(5),
					content: rs.getString(6)
				])
			}
		}
		finally {
			rs.close()
		}
	}
	finally {
		statement.close()
	}

	return notes
}

addNote = { note, connection ->
	note.timestamp = System.currentTimeMillis()
	def statement = connection.prepareStatement('INSERT INTO note (board, x, y, size, content, timestamp) VALUES (?, ?, ?, ?, ?, ?)')
	try {
		statement.setString(1, note.board)
		statement.setInt(2, note.x)
		statement.setInt(3, note.y)
		statement.setInt(4, note.size)
		statement.setString(5, note.content)
		statement.setTimestamp(6, new Timestamp(note.timestamp))
		statement.execute()
	}
	finally {
		statement.close()
	}
}

updateNote = { note, connection ->
    // TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    // there is a chance that the board will not be set with the latest timestamp!

	note.timestamp = System.currentTimeMillis()
	def statement = connection.prepareStatement('UPDATE note SET board=?, x=?, y=?, size=?, content=?, timestamp=? WHERE id=?')
	try {
		statement.setString(1, note.board)
		statement.setInt(2, note.x)
		statement.setInt(3, note.y)
		statement.setInt(4, note.size)
		statement.setString(5, note.content)
		statement.setTimestamp(6, new Timestamp(note.timestamp))
		statement.setInt(7, note.id)
		statement.execute()
	}
	finally {
		statement.close()
	}
}

deleteNote = { note, connection ->
	statement = connection.prepareStatement('DELETE FROM note WHERE id=?')
	try {
		statement.setInt(1, note.id)
		statement.execute()
	}
	finally {
		statement.close()
	}
}
