
set java=java
set main=com.threecrickets.scripturian.ScriptedMain

set jars=lib/com.sun.grizzly.jar;^
lib/com.sun.phobos.script.javascript.jar;^
lib/com.caucho.quercus.jar;^
lib/com.caucho.resin.util.jar;^
lib/com.sun.script.jruby.jar;^
lib/com.sun.script.velocity.jar;^
lib/com.threecrickets.prudence.jar;^
lib/com.threecrickets.scripturian.jar;^
lib/javax.script.jar;^
lib/javax.servlet.jar;^
lib/jep.jar;^
lib/jython.jar;^
lib/jython-engine.jar;^
lib/org.apache.log4j.jar;^
lib/org.apache.velocity.jar;^
lib/org.codehaus.groovy.jar;^
lib/org.jruby.jar;^
lib/org.json.jar;^
lib/org.mozilla.javascript.jar;^
lib/org.restlet.ext.grizzly.jar;^
lib/org.restlet.ext.json.jar;^
lib/org.restlet.ext.slf4j.jar;^
lib/org.restlet.jar;^
lib/org.slf4j.bridge.jar;^
lib/org.slf4j.impl.jar;^
lib/org.slf4j.jar

%java% -cp %jars% %main%
