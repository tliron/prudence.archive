
set java=java
set main=com.threecrickets.scripturian.Scripturian

set jars=lib/clojure.jar:^
lib/clojure.contrib.jar:^
lib/clojure.contrib.jsr223.jar:^
lib/com.mysql.jdbc.jar;^
lib/com.sun.grizzly.jar;^
lib/com.sun.script.velocity.jar;^
lib/com.threecrickets.jygments.jar;^
lib/com.threecrickets.prudence.jar;^
lib/com.threecrickets.scripturian.jar;^
lib/javax.script.jar;^
lib/org.apache.log4j.jar;^
lib/org.apache.velocity.jar;^
lib/org.codehaus.jackson.jar:^
lib/org.codehaus.jackson.mapper.jar:^
lib/org.json.jar;^
lib/org.restlet.ext.grizzly.jar;^
lib/org.restlet.ext.jackson.jar:^
lib/org.restlet.ext.json.jar;^
lib/org.restlet.ext.slf4j.jar;^
lib/org.restlet.jar;^
lib/org.slf4j.bridge.jar;^
lib/org.slf4j.impl.jar;^
lib/org.slf4j.jar

%java% -cp %jars% %main% instance
