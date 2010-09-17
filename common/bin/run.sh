#!/bin/bash
set -e

JARS=\
#foreach($jar in $jars.split(':'))
libraries/${jar}#if($velocityHasNext):\
#end
#end


PID=/tmp/prudence-${distribution}.pid
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
#if(($distribution == 'python') || ($distribution == 'kitchensink'))
	-Dpython.home=libraries/python \
	-Dpython.verbose=warning \
	-Djava.library.path=/usr/local/lib \
#end
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

	if ! id prudence-${distribution} > /dev/null 2>&1; then
		echo "User 'prudence-${distribution}' must exist to start Prudence"
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
	-user prudence-${distribution} \
	-procname prudence \
	-outfile "$HERE/../logs/run.log" \
	-errfile '&1' \
	-cp "$JARS" \
	-Dscripturian.cache=cache \
#if(($distribution == 'python') || ($distribution == 'kitchensink'))
	-Dpython.home=libraries/python \
	-Dpython.verbose=warning \
	-Djava.library.path=/usr/local/lib \
#end
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
