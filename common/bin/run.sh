#!/bin/bash
#
# Copyright 2009-2010 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

set -e

JARS=\
#foreach($jar in $jars.split(':'))
libraries/${jar}#if($velocityHasNext):\
#end
#end


set +e
SCRIPT=$(readlink -f "$0" 2>/dev/null)
if [ "$?" == 0 ]; then
	set -e
	HERE=$(dirname "$(readlink -f "$SCRIPT")")
else
	set -e
	# "readlink -f" isn't supported on all platforms
	SCRIPT="$0"
	OLD_PWD="$PWD"
	cd $(dirname "$SCRIPT")
	HERE="$PWD"
	cd "$OLD_PWD"
fi

OS=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$(uname -m)
JSVC=/usr/bin/jsvc
PID=/tmp/prudence-${distribution}.pid
DAEMON_USER=prudence-${distribution}
LOGS="$HERE/../logs"

if [ ! -f "$JSVC" ]; then
	# Use our jsvc binary
	if [ "$OS" == 'darwin' ]; then
		# Darwin has a universal binary
		JSVC="$HERE/commons-daemon/$OS/jsvc"
	else
		JSVC="$HERE/commons-daemon/$OS/$ARCH/jsvc"
	fi
fi

if [ -z "$JAVA_HOME" ]; then
	# Common defaults for JAVA_HOME
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
		echo You must have Apache Commons Daemon installed in order to start Prudence as a daemon
		exit 1
	fi

	if [ $EUID -ne 0 ]; then
		echo You must have root privileges in order to start Prudence
		exit 1
	fi

	if [ -f "$PID" ]; then
		echo "Prudence is already running (pid $(cat "$PID"))"
		exit 1
	fi

	if [ -z "$JAVA_HOME" ]; then
		echo You must correctly set JAVA_HOME in order to start Prudence
		exit 1
	fi

	if ! id "$DAEMON_USER" > /dev/null 2>&1; then
		# Default to executing user
		DAEMON_USER="$SUDO_USER"
		if [ -z "$DAEMON_USER" ]; then
			DAEMON_USER="$USER"
		fi
	fi
	
	if [ ! -d "$LOGS" ]; then
		mkdir -p "$LOGS"
		DAEMON_USER_ID=$(id -u "$DAEMON_USER")
		chown $DAEMON_USER_ID "$LOGS"
	fi

	echo Starting Prudence with user \"$DAEMON_USER\"...

	cd "$HERE/.."

	"$JSVC" \
	-home "$JAVA_HOME" \
	-pidfile "$PID" \
	-user "$DAEMON_USER" \
	-procname prudence \
	-outfile "$LOGS/run.log" \
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
		echo You must have Apache Commons Daemon installed in order to stop Prudence
		exit 1
	fi

	if [ $EUID -ne 0 ]; then
		echo You must have root privileges in order to stop Prudence
		exit 1
	fi

	if [ ! -f "$PID" ]; then
		echo "Prudence is not running"
		exit 1
	fi

	echo Stopping Prudence...

	"$JSVC" \
	-pidfile "$PID" \
	-outfile "$LOGS/run.log" \
	-errfile '&1' \
	-stop \
	com.threecrickets.prudence.PrudenceDaemon
}

status () {
	if [ $EUID -ne 0 ]; then
		echo "You must be have root privileges in order to check Prudence's status"
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
