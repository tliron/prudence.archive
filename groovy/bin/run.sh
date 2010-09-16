#!/bin/bash
set -e

JARS=\
libraries/com.hazelcast.jar:\
libraries/com.mysql.jdbc.jar:\
libraries/com.sun.grizzly.jar:\
libraries/com.threecrickets.jygments.jar:\
libraries/com.threecrickets.prudence.jar:\
libraries/com.threecrickets.scripturian.jar:\
libraries/com.threecrickets.succinct.jar:\
libraries/it.sauronsoftware.cron4j.jar:\
libraries/javax.servlet.jar:\
libraries/net.jcip.annotations.jar:\
libraries/net.spy.memcached.jar:\
libraries/org.apache.commons.codec.jar:\
libraries/org.apache.commons.dbcp.jar:\
libraries/org.apache.commons.fileupload.jar:\
libraries/org.apache.commons.io.jar:\
libraries/org.apache.commons.logging.jar:\
libraries/org.apache.commons.pool.jar:\
libraries/org.apache.http.jar:\
libraries/org.apache.http.client.jar:\
libraries/org.apache.http.entity.mime.jar:\
libraries/org.apache.james.mime4j.jar:\
libraries/org.apache.log4j.jar:\
libraries/org.apache.velocity.jar:\
libraries/org.codehaus.groovy.jar:\
libraries/org.codehaus.jackson.jar:\
libraries/org.codehaus.jackson.mapper.jar:\
libraries/org.h2.jar:\
libraries/org.json.jar:\
libraries/org.restlet.ext.fileupload.jar:\
libraries/org.restlet.ext.grizzly.jar:\
libraries/org.restlet.ext.httpclient.jar:\
libraries/org.restlet.ext.jackson.jar:\
libraries/org.restlet.ext.json.jar:\
libraries/org.restlet.ext.slf4j.jar:\
libraries/org.restlet.jar:\
libraries/org.slf4j.bridge.jar:\
libraries/org.slf4j.impl.jar:\
libraries/org.slf4j.jar

PID=/tmp/prudence-groovy.pid
JSVC=/usr/bin/jsvc
SCRIPT=$(readlink -f "$0")
HERE=$(readlink -f "$(dirname "$SCRIPT")")

if [ -z "$JAVA_HOME" ]; then
	JAVA_HOME=/usr/lib/jvm/java-6-openjdk
	#JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun
fi

JAVA="$JAVA_HOME/bin/java"

if [ ! -f "$JAVA" ]; then
	JAVA=/usr/bin/java
fi

cd "$HERE/.."

console () {
	if [ ! -f "$JAVA" ]; then
		echo You must correctly set JAVA_HOME to start Prudence
		exit 1
	fi
	
	exec \
	"$JAVA" \
	-cp "$JARS" \
	-Dscripturian.cache=cache \
	com.threecrickets.scripturian.Scripturian instance
}

start () {
	if [ ! -f "$JSVC" ]; then
		echo Prudence can only be started in console mode on this system
		exit 1
	fi

	if [ $EUID -ne 0 ]; then
		echo You must have root privileges to start Prudence
		exit 1
	fi
	
	if [ -f "$PID" ]; then
		echo "Prudence is already running (pid $(cat "$PID"))"
		exit 1
	fi

	if ! id prudence > /dev/null 2>&1; then
		echo "User 'prudence' must exist to start Prudence"
		exit 1
	fi

	if [ -z "$JAVA_HOME" ]; then
		echo You must correctly set JAVA_HOME to start Prudence
		exit 1
	fi

	echo Starting Prudence...
	"$JSVC" \
	-home "$JAVA_HOME" \
	-pidfile "$PID" \
	-user prudence \
	-procname prudence \
	-outfile "$HERE/../logs/run.log" \
	-errfile '&1' \
	-cp "$JARS" \
	-Dscripturian.cache=cache \
	com.threecrickets.prudence.PrudenceDaemon instance
}

stop () {
	if [ $EUID -ne 0 ]; then
		echo You must be have root privileges to stop Prudence
		exit 1
	fi

	if [ ! -f "$PID" ]; then
		echo "Prudence is not running"
		exit 1
	fi

	echo Stopping Prudence...
	"$JSVC" \
	-pidfile "$PID" \
	-outfile "$HERE/../logs/run.log" \
	-errfile '&1' \
	-stop \
	com.threecrickets.prudence.PrudenceDaemon
}

status () {
	if [ $EUID -ne 0 ]; then
		echo "You must be have root privileges to check Prudence's status"
		exit 1
	fi

	if [ -f "$PID" ]; then
		echo "Prudence is running (pid $(cat "$PID"))"
	else
		echo Prudence is not running
	fi
}

case "$1" in
	console)
		console
		;;
	start)
		start
		;;
	stop)
		stop
		;;
	restart)
		stop
		start
		;;
	status)
		status
		;;
	*)
		echo "Usage: {console|start|stop|restart|status}"
		exit 1
		;; 
esac
