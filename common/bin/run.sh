#!/bin/bash
set -e

JARS=\
#foreach($jar in $jars.split(':'))
libraries/${jar}#if($velocityHasNext):\
#end
#end


PID=/tmp/prudence-${distribution}.pid
HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$(uname -m)

if [ "$OS" == 'darwin' ]; then
	# Darwin has a universal binary
	JSVC="$HERE/commons-daemon/$OS/jsvc"
else
	JSVC="$HERE/commons-daemon/$OS/$ARCH/jsvc"
fi

if [ -z "$JAVA_HOME" ]; then
	if [ "$OS" == 'darwin' ]; then
		JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
	else
		JAVA_HOME=/usr/lib/jvm/java-6-openjdk
	fi
fi

JAVA="$JAVA_HOME/bin/java"

if [ ! -f "$JAVA" ]; then
	JAVA=/usr/bin/java
fi

console () {
	if [ ! -f "$JAVA" ]; then
		echo You must correctly set JAVA_HOME or have a /usr/bin/java to start Prudence in console mode
		exit 1
	fi
	
	cd "$HERE/.."

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
	
	cd "$HERE/.."

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
	if [ ! -f "$JSVC" ]; then
		echo Prudence can only be started in console mode on this system
		exit 1
	fi

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
	if [ ! -f "$JSVC" ]; then
		echo Prudence can only be started in console mode on this system
		exit 1
	fi

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
