/**
 * This version of MiniConnectionPoolManager has been modified by Daniel Jurado
 * <p>
 * Copyright 2007 Christian d'Heureuse, www.source-code.biz
 * <p>
 * This module is multi-licensed and may be used under the terms of any of the
 * following licenses:
 * <p>
 * EPL, Eclipse Public License, http://www.eclipse.org/legal<br>
 * LGPL, GNU Lesser General Public License,
 * http://www.gnu.org/licenses/lgpl.html<br>
 * MPL, Mozilla Public License 1.1, http://www.mozilla.org/MPL
 * <p>
 * This module is provided "as is", without warranties of any kind.
 */

package com.threecrickets.prudence.util;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.EmptyStackException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * A simple standalone JDBC connection pool manager.
 * <p>
 * The public methods of this class are thread-safe.
 * <p>
 * Author: Christian d'Heureuse (<a
 * href="http://www.source-code.biz">www.source-code.biz</a>)<br>
 * Multi-licensed: EPL/LGPL/MPL.
 * <p>
 * 2007-06-21: Constructor with a timeout parameter added.<br>
 * 2008-05-03: Additional licenses added (EPL/MPL). 2009-06-24 Daniel Jurado:
 * Changed from a Stack to a Queue so it is possible do recycle more
 * efficiently.<br>
 * 2009-06-25 Daniel Jurado: Timeout to handle and idle connection.<br>
 * 2009-12-07 Carlos Vizcaino: Shutting timer on dispose, timer was preventing
 * Tomcat from shutting down within 60 secs.
 */
public class MiniConnectionPoolManager
{

	/**
	 * Object to hold the Connection under the queue, with a TimeStamp.
	 * 
	 * @author Daniel Jurado
	 */
	private class PCTS implements Comparable<PCTS>
	{
		private PooledConnection pconn;

		private Calendar timeStamp;

		private PooledConnection getPConn()
		{
			return this.pconn;
		}

		private Calendar getTimeStamp()
		{
			return this.timeStamp;
		}

		private PCTS( PooledConnection pconn )
		{
			this.timeStamp = Calendar.getInstance();
			this.pconn = pconn;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo( PCTS other )
		{
			return (int) ( other.getTimeStamp().getTimeInMillis() - this.getTimeStamp().getTimeInMillis() );
		}
	}

	private class PoolConnectionEventListener implements ConnectionEventListener
	{
		public void connectionClosed( ConnectionEvent event )
		{
			PooledConnection pconn = (PooledConnection) event.getSource();
			pconn.removeConnectionEventListener( this );
			recycleConnection( pconn );
		}

		public void connectionErrorOccurred( ConnectionEvent event )
		{
			PooledConnection pconn = (PooledConnection) event.getSource();
			pconn.removeConnectionEventListener( this );
			disposeConnection( pconn );
		}
	}

	/**
	 * Looks for recycled connections older than the specified seconds.
	 * 
	 * @author Daniel Jurado
	 */
	private class ConnectionMonitor extends TimerTask
	{
		private MiniConnectionPoolManager owner;

		private ConnectionMonitor( MiniConnectionPoolManager owner )
		{
			this.owner = owner;
		}

		@Override
		public void run()
		{
			Calendar now = Calendar.getInstance();
			synchronized( owner )
			{
				for( PCTS pcts : recycledConnections )
				{
					int delta = (int) ( ( now.getTimeInMillis() - pcts.getTimeStamp().getTimeInMillis() ) / 1000 );
					if( delta >= maxIdleConnectionLife )
					{
						closeConnectionNoEx( pcts.getPConn() );
						recycledConnections.remove( pcts );
					}
				}
			}
		}
	}

	/**
	 * Thrown in {@link #getConnection()} when no free connection becomes
	 * available within <code>timeout</code> seconds.
	 */
	public static class TimeoutException extends RuntimeException
	{
		private static final long serialVersionUID = 1;

		public TimeoutException()
		{
			super( "Timeout while waiting for a free database connection." );
		}
	}

	private ConnectionPoolDataSource dataSource;

	private int maxConnections;

	private int maxIdleConnectionLife;

	private int timeout;

	private Semaphore semaphore;

	private Queue<PCTS> recycledConnections;

	private int activeConnections;

	private PoolConnectionEventListener poolConnectionEventListener;

	private boolean isDisposed;

	private Timer timer;

	/**
	 * Constructs a MiniConnectionPoolManager object with a timeout of 60
	 * seconds and 60 seconds of max idle connection life.
	 * 
	 * @param dataSource
	 *        the data source for the connections.
	 * @param maxConnections
	 *        the maximum number of connections.
	 */
	public MiniConnectionPoolManager( ConnectionPoolDataSource dataSource, int maxConnections )
	{
		this( dataSource, maxConnections, 60, 60 );
	}

	/**
	 * Constructs a MiniConnectionPoolManager object.
	 * 
	 * @param dataSource
	 *        the data source for the connections.
	 * @param maxConnections
	 *        the maximum number of connections.
	 * @param timeout
	 *        the maximum time in seconds to wait for a free connection.
	 * @param maxIdleConnectionLife
	 *        the maximum time in seconds to keep an idle connection to wait to
	 *        be used.
	 */
	public MiniConnectionPoolManager( ConnectionPoolDataSource dataSource, int maxConnections, int timeout, int maxIdleConnectionLife )
	{
		if( dataSource == null )
			throw new InvalidParameterException( "dataSource cant be null" );
		if( maxConnections < 1 )
			throw new InvalidParameterException( "maxConnections must be > 1" );
		if( timeout < 1 )
			throw new InvalidParameterException( "timeout must be > 1" );
		if( maxIdleConnectionLife < 1 )
			throw new InvalidParameterException( "maxIdleConnectionLife must be > 1" );

		this.dataSource = dataSource;
		this.maxConnections = maxConnections;
		this.maxIdleConnectionLife = maxIdleConnectionLife;
		this.timeout = timeout;
		semaphore = new Semaphore( maxConnections, true );
		recycledConnections = new PriorityQueue<PCTS>();
		poolConnectionEventListener = new PoolConnectionEventListener();

		// start the monitor
		timer = new Timer( getClass().getSimpleName(), true );
		timer.schedule( new ConnectionMonitor( this ), this.maxIdleConnectionLife, this.maxIdleConnectionLife );
	}

	private void assertInnerState()
	{
		if( activeConnections < 0 )
			throw new AssertionError();
		if( activeConnections + recycledConnections.size() > maxConnections )
			throw new AssertionError();
		if( activeConnections + semaphore.availablePermits() > maxConnections )
			throw new AssertionError();
	}

	private void closeConnectionNoEx( PooledConnection pconn )
	{
		try
		{
			pconn.close();
		}
		catch( SQLException e )
		{
			log( "Error while closing database connection: " + e.toString() );
		}
	}

	/**
	 * Closes all unused pooled connections.
	 */
	public synchronized void dispose() throws SQLException
	{
		if( isDisposed )
			return;
		isDisposed = true;
		SQLException e = null;
		while( !recycledConnections.isEmpty() )
		{
			PCTS pcts = recycledConnections.poll();
			if( pcts == null )
				throw new EmptyStackException();
			PooledConnection pconn = pcts.getPConn();
			try
			{
				pconn.close();
			}
			catch( SQLException e2 )
			{
				if( e == null )
					e = e2;
			}
		}
		timer.cancel();
		if( e != null )
			throw e;
	}

	private synchronized void disposeConnection( PooledConnection pconn )
	{
		if( activeConnections < 0 )
			throw new AssertionError();
		activeConnections--;
		semaphore.release();
		closeConnectionNoEx( pconn );
		assertInnerState();
	}

	/**
	 * Returns the number of active (open) connections of this pool. This is the
	 * number of <code>Connection</code> objects that have been issued by
	 * {@link #getConnection()} for which <code>Connection.close()</code> has
	 * not yet been called.
	 * 
	 * @return the number of active connections.
	 **/
	public synchronized int getActiveConnections()
	{
		return activeConnections;
	}

	/**
	 * Retrieves a connection from the connection pool. If
	 * <code>maxConnections</code> connections are already in use, the method
	 * waits until a connection becomes available or <code>timeout</code>
	 * seconds elapsed. When the application is finished using the connection,
	 * it must close it in order to return it to the pool.
	 * 
	 * @return a new Connection object.
	 * @throws TimeoutException
	 *         when no connection becomes available within <code>timeout</code>
	 *         seconds.
	 */
	public Connection getConnection() throws SQLException
	{
		// This routine is unsynchronized, because semaphore.tryAcquire() may
		// block.
		synchronized( this )
		{
			if( isDisposed )
				throw new IllegalStateException( "Connection pool has been disposed." );
		}
		try
		{
			if( !semaphore.tryAcquire( timeout, TimeUnit.SECONDS ) )
				throw new TimeoutException();
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( "Interrupted while waiting for a database connection.", e );
		}
		boolean ok = false;
		try
		{
			Connection conn = getConnection2();
			ok = true;
			return conn;
		}
		finally
		{
			if( !ok )
				semaphore.release();
		}
	}

	private synchronized Connection getConnection2() throws SQLException
	{
		if( isDisposed )
			throw new IllegalStateException( "Connection pool has been disposed." ); // test
		// again
		// with
		// lock
		PooledConnection pconn;
		if( recycledConnections.size() > 0 )
		{
			PCTS pcts = recycledConnections.poll();
			if( pcts == null )
				throw new EmptyStackException();
			pconn = pcts.getPConn();

		}
		else
		{
			pconn = dataSource.getPooledConnection();
		}
		Connection conn = pconn.getConnection();
		activeConnections++;
		pconn.addConnectionEventListener( poolConnectionEventListener );
		assertInnerState();
		return conn;
	}

	private void log( String msg )
	{
		Logger.getLogger( getClass().getName() ).severe( msg );
	}

	private synchronized void recycleConnection( PooledConnection pconn )
	{
		if( isDisposed )
		{
			disposeConnection( pconn );
			return;
		}
		if( activeConnections <= 0 )
			throw new AssertionError();
		activeConnections--;
		semaphore.release();
		recycledConnections.add( new PCTS( pconn ) );
		assertInnerState();
	}
}
