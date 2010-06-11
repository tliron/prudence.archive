
importClass(
	java.lang.System,
	java.lang.Class,
	java.util.concurrent.locks.ReentrantLock,
	java.sql.DriverManager,
	java.sql.SQLException,
	java.sql.Timestamp,
	org.apache.commons.pool.impl.GenericObjectPool,
	org.apache.commons.dbcp.DataSourceConnectionFactory,
	org.apache.commons.dbcp.PoolableConnectionFactory,
	org.apache.commons.dbcp.PoolingDataSource);

var connectionPoolLock = application.globals.get('connectionPoolLock');
if(connectionPoolLock == null) {
	connectionPoolLock = application.getGlobal('connectionPoolLock', new ReentrantLock());
}

function getDataSource() {
	var dataSource;
	if(application.globals.get('stickstick.backend') == 'h2') {
		dataSource = new org.h2.jdbcx.JdbcDataSource();
	} else if(application.globals.get('stickstick.backend') == 'mysql') {
		dataSource = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
	}
	dataSource.URL = getUrl();
	dataSource.user = application.globals.get('stickstick.username');
	dataSource.password = application.globals.get('stickstick.password');
	return dataSource;
}

function getUrl() {
	var url = 'jdbc:' + application.globals.get('stickstick.backend') + ':';
	if(application.globals.get('stickstick.host') != '') {
		if(application.globals.get('stickstick.backend') == 'h2') {
			url += 'tcp:';
		}
		url += '//' + application.globals.get('stickstick.host') + '/';
	}
	if(application.globals.get('stickstick.database') != '') {
		url += application.globals.get('stickstick.database');
	}
	return url;
}

function getConnectionPool() {
	var connectionPool = new GenericObjectPool(null, 10);
	new PoolableConnectionFactory(new DataSourceConnectionFactory(getDataSource()), connectionPool, null, null, false, true);
	return new PoolingDataSource(connectionPool);
}

function getConnection(fresh) {
	connectionPoolLock.lock();
	connectionPool = application.globals.get('connectionPool');
	try {
		if(connectionPool == null || fresh) {
			if(connectionPool == null) {
				connectionPool = application.getGlobal('connectionPool', getConnectionPool());
			}
			
			// TODO: CREATE DATABASE IF NOT EXISTS
	
			var connection = connectionPool.connection;
	
			if(fresh) {
				var statement = connection.createStatement();
				try {
					statement.execute('DROP TABLE board');
					statement.execute('DROP TABLE note');
				}
				catch(e if e.javaException instanceof SQLException) {
					// Tables already gone
				}
				finally {
					statement.close();
				}
			}
	
			var statement = connection.createStatement();
			try {
				statement.execute('CREATE TABLE IF NOT EXISTS board (id VARCHAR(50) PRIMARY KEY, timestamp TIMESTAMP)');
				statement.execute('CREATE TABLE IF NOT EXISTS note (id INT AUTO_INCREMENT PRIMARY KEY, board VARCHAR(50), x INT, y INT, size INT, content TEXT, timestamp TIMESTAMP)');
				statement.execute('CREATE INDEX IF NOT EXISTS note_board_idx ON note (board)');
			}
			catch(e if e.javaException instanceof SQLException) {
				// Already exist
			}
			finally {
				statement.close();
			}
			
			try {
				addBoard({id: 'Todo List'}, connection);
				addBoard({id: 'Great Ideas'}, connection);
				addBoard({id: 'Sandbox'}, connection);
				//addNote({board: 'Todo', x: 100, y: 100, size: 0, content: 'Test!'}, connection);
			}
			catch(e if e.javaException instanceof SQLException) {
				// Boards already exist
			}
	
			connection.close();
		}
		
		return connectionPool.connection;
	}
	finally {
		connectionPoolLock.unlock();
	}
}

function getBoards(connection) {
	var boards = [];

	var statement = connection.createStatement();
	try {
		var rs = statement.executeQuery('SELECT id, timestamp FROM board');
		try {
			while(rs.next()) {
				boards.push({
					id: String(rs.getString(1)),
					timestamp: rs.getTimestamp(2).time
				});
			}
		}
		finally {
			rs.close();
		}
	}
	finally {
		statement.close();
	}

	return boards;
}

function addBoard(board, connection) {
	var statement = connection.prepareStatement('INSERT INTO board (id, timestamp) VALUES (?, ?)');
	try {
		statement.setString(1, board.id);
		statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		statement.execute();
	}
	finally {
		statement.close();
	}
}

function updateBoardTimestamp(note, connection, timestamp) {
    // TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    // there is a chance that the board will not be set with the latest timestamp!

    if(timestamp == null) {
		timestamp = note.timestamp;
	}

	var statement = connection.prepareStatement('UPDATE board SET timestamp=? WHERE id=?');
	try {
		statement.setTimestamp(1, new Timestamp(timestamp));
		statement.setString(2, note.board);
		statement.execute();
	}
	finally {
		statement.close();
	}
}

function getBoardMaxTimestamp(connection) {
	var statement = connection.createStatement();
	try {
		var rs = statement.executeQuery('SELECT MAX(timestamp) FROM BOARD');
		if(rs.next()) {
			return rs.getTimestamp(1).time;
		}
	}
	finally {
		statement.close();
	}
	
	return null;
}

function getNote(id, connection) {
	if(!id) {
		return null;
	}
	
	var statement = connection.prepareStatement('SELECT board, x, y, size, content, timestamp FROM note WHERE id=?');
	try {
		statement.setInt(1, id);
		var rs = statement.executeQuery();
		try {
			if(rs.next()) {
				return {
					id: id,
					board: String(rs.getString(1)),
					x: rs.getInt(2),
					y: rs.getInt(3),
					size: rs.getInt(4),
					content: String(rs.getString(5)),
					timestamp: rs.getTimestamp(6).time
				};
			}
		}
		finally {
			rs.close();
		}
	}
	finally {
		statement.close();
	}

	return null;
}

function getNotes(connection) {
	var notes = [];

	var statement = connection.createStatement();
	try {
		var rs = statement.executeQuery('SELECT id, board, x, y, size, content FROM note');
		try {
			while(rs.next()) {
				notes.push({
					id: rs.getInt(1),
					board: String(rs.getString(2)),
					x: rs.getInt(3),
					y: rs.getInt(4),
					size: rs.getInt(5),
					content: String(rs.getString(6))
				});
			}
		}
		finally {
			rs.close();
		}
	}
	finally {
		statement.close();
	}

	return notes;
}

function addNote(note, connection) {
	note.timestamp = System.currentTimeMillis();
	var statement = connection.prepareStatement('INSERT INTO note (board, x, y, size, content, timestamp) VALUES (?, ?, ?, ?, ?, ?)');
	try {
		statement.setString(1, note.board);
		statement.setInt(2, note.x);
		statement.setInt(3, note.y);
		statement.setInt(4, note.size);
		statement.setString(5, note.content);
		statement.setTimestamp(6, new Timestamp(note.timestamp));
		statement.execute();
	}
	finally {
		statement.close();
	}
}

function updateNote(note, connection) {
    // TODO: we are not guaranteeing atomicity here! In a highly concurrent environment,
    // there is a chance that the board will not be set with the latest timestamp!

	note.timestamp = System.currentTimeMillis();
	var statement = connection.prepareStatement('UPDATE note SET board=?, x=?, y=?, size=?, content=?, timestamp=? WHERE id=?');
	try {
		statement.setString(1, note.board);
		statement.setInt(2, note.x);
		statement.setInt(3, note.y);
		statement.setInt(4, note.size);
		statement.setString(5, note.content);
		statement.setTimestamp(6, new Timestamp(note.timestamp));
		statement.setInt(7, note.id);
		statement.execute();
	}
	finally {
		statement.close();
	}
}

function deleteNote(note, connection) {
	statement = connection.prepareStatement('DELETE FROM note WHERE id=?');
	try {
		statement.setInt(1, note.id);
		statement.execute();
	}
	finally {
		statement.close();
	}
}
