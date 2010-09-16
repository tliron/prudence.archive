@echo off
cd /d %0%\..\..

set JAVA=java

set JARS=#foreach($jar in $jars.split(':'))
libraries/${jar}#if($velocityHasNext);^
#end
#end


%JAVA% ^
-cp %JARS% ^
-Dscripturian.cache=cache ^
com.threecrickets.scripturian.Scripturian instance
