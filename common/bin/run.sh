#!/bin/bash

here=$(readlink -f "$(dirname "$0")")
cd $here/..

java=/usr/bin/java
#java=/usr/libraries/jvm/java-1.5.0-sun/bin/java

main=com.threecrickets.scripturian.Scripturian

jars=\
libraries/clojure.jar:\
libraries/clojure.contrib.jar:\
libraries/com.hazelcast.jar:\
libraries/com.sun.grizzly.jar:\
libraries/com.sun.phobos.script.javascript.jar:\
libraries/com.caucho.quercus.jar:\
libraries/com.caucho.resin.util.jar:\
libraries/com.sun.script.velocity.jar:\
libraries/com.threecrickets.jygments.jar:\
libraries/com.threecrickets.prudence.jar:\
libraries/com.threecrickets.scripturian.jar:\
libraries/it.sauronsoftware.cron4j.jar:\
libraries/javax.script.jar:\
libraries/javax.servlet.jar:\
libraries/jep.jar:\
libraries/net.spy.memcached.jar:\
libraries/org.apache.log4j.jar:\
libraries/org.apache.velocity.jar:\
libraries/org.codehaus.groovy.jar:\
libraries/org.h2.jar:\
libraries/org.jruby.jar:\
libraries/org.json.jar:\
libraries/org.mozilla.javascript.jar:\
libraries/org.python.jar:\
libraries/org.restlet.ext.grizzly.jar:\
libraries/org.restlet.ext.json.jar:\
libraries/org.restlet.ext.slf4j.jar:\
libraries/org.restlet.jar:\
libraries/org.slf4j.bridge.jar:\
libraries/org.slf4j.impl.jar:\
libraries/org.slf4j.jar

# We are setting java.library.path for Jepp

"$java" \
-cp "$jars" \
-Dscripturian.cache=cache \
-Dpython.home=libraries/python \
-Dpython.verbose=warning \
-Djava.library.path=/usr/local/lib \
$main instance
