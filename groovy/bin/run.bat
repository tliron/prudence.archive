
set java=java
set main=com.threecrickets.scripturian.Scripturian

set jars=libraries/com.mysql.jdbc.jar;^
libraries/com.sun.grizzly.jar;^
libraries/com.sun.script.velocity.jar;^
libraries/com.threecrickets.jygments.jar;^
libraries/com.threecrickets.prudence.jar;^
libraries/com.threecrickets.scripturian.jar;^
libraries/javax.script.jar;^
libraries/org.apache.log4j.jar;^
libraries/org.apache.velocity.jar;^
libraries/org.codehaus.groovy.jar;^
libraries/org.codehaus.jackson.jar;^
libraries/org.codehaus.jackson.mapper.jar;^
libraries/org.h2.jar;^
libraries/org.json.jar;^
libraries/org.restlet.ext.grizzly.jar;^
libraries/org.restlet.ext.jackson.jar;^
libraries/org.restlet.ext.json.jar;^
libraries/org.restlet.ext.slf4j.jar;^
libraries/org.restlet.jar;^
libraries/org.slf4j.bridge.jar;^
libraries/org.slf4j.impl.jar;^
libraries/org.slf4j.jar

%java% ^
-cp %jars% ^
-Dscripturian.cache=cache ^
%main% instance
