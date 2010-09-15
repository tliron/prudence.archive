#!/bin/bash
set -e

SCRIPT=$(readlink -f "$0")
HERE=$(readlink -f "$(dirname "$SCRIPT")")
cd "$HERE/.."

JAVA=/usr/bin/java
#JAVA=/usr/lib/jvm/java-1.5.0-sun/bin/java

MAIN=com.threecrickets.scripturian.Scripturian

JARS=\
libraries/com.caucho.quercus.jar:\
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

exec "$JAVA" \
-cp "$JARS" \
-Dscripturian.cache=cache \
$MAIN instance
