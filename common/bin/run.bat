
set java=java
set main=com.threecrickets.scripturian.Scripturian

set jars=libraries/clojure.jar;^
libraries/clojure.contrib.jar;^
libraries/clojure.contrib.jsr223.jar;^
libraries/com.sun.grizzly.jar;^
libraries/com.sun.phobos.script.javascript.jar;^
libraries/com.caucho.quercus.jar;^
libraries/com.caucho.resin.util.jar;^
libraries/com.sun.script.velocity.jar;^
libraries/com.threecrickets.jygments.jar;^
libraries/com.threecrickets.prudence.jar;^
libraries/com.threecrickets.scripturian.jar;^
libraries/javax.script.jar;^
libraries/javax.servlet.jar;^
libraries/jep.jar;^
libraries/org.apache.log4j.jar;^
libraries/org.apache.velocity.jar;^
libraries/org.codehaus.groovy.jar;^
libraries/org.jruby.jar;^
libraries/org.json.jar;^
libraries/org.mozilla.javascript.jar;^
libraries/org.python.jar;^
libraries/org.restlet.ext.grizzly.jar;^
libraries/org.restlet.ext.json.jar;^
libraries/org.restlet.ext.slf4j.jar;^
libraries/org.restlet.jar;^
libraries/org.slf4j.bridge.jar;^
libraries/org.slf4j.impl.jar;^
libraries/org.slf4j.jar

%java% ^
-cp %jars% ^
-Djava.library.path=/usr/local/lib ^
-Dpython.home=libraries/python ^
-Dpython.cachedir=../../cache/python ^
-Dpython.verbose=warning ^
%main% instance
