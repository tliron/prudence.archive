@ECHO OFF
REM #
REM # Copyright 2009-2010 Three Crickets LLC.
REM #
REM # The contents of this file are subject to the terms of the LGPL version 3.0:
REM # http://www.opensource.org/licenses/lgpl-3.0.html
REM #
REM # Alternatively, you can obtain a royalty free commercial license with less
REM # limitations, transferable or non-transferable, directly from Three Crickets
REM # at http://threecrickets.com/
REM #

CD /D "%0%\..\.."

SET JARS=#foreach($jar in $jars.split(':'))
%CD%/libraries/${jar}#if($velocityHasNext);^
#end
#end


SET JAVA=java
SET PRUNSRV="%CD%\bin\commons-daemon\windows\i386\prunsrv.exe"
SET PRUNMGR="%CD%\bin\commons-daemon\windows\prunmgr.exe"

IF "%1"=="console" GOTO console
IF "%1"=="start" GOTO start
IF "%1"=="stop" GOTO stop
IF "%1"=="install" GOTO install
IF "%1"=="uninstall" GOTO uninstall
IF "%1"=="monitor" GOTO monitor
IF "%1"=="settings" GOTO settings
ECHO Usage: {console^|start^|stop^|install^|uninstall^|monitor^|settings}
EXIT /B

:console

%JAVA% ^
-cp %JARS% ^
-Dscripturian.cache=cache ^
com.threecrickets.scripturian.Scripturian instance
EXIT /B

:install

ECHO Installing service Prudence${service}... 

%PRUNSRV% ^
//IS//Prudence${service} ^
--DisplayName="Prudence for ${service}" ^
--Description="Prudence for ${service}" ^
--LogPath="%CD%/logs" ^
--LogPrefix=service ^
--LogLevel=Debug ^
--LogJniMessages=1 ^
--PidFile=run.pid ^
--StdOutput=auto ^
--StdError=auto ^
--StartMode=jvm ^
--StartClass=com.threecrickets.prudence.PrudenceDaemon ^
--StartParams=--base-path="%CD%";instance ^
--StopMode=jvm ^
--StopClass=com.threecrickets.prudence.PrudenceDaemon ^
--StopMethod=stop ^
--Jvm=auto ^
--JvmOptions=-Dscripturian.cache=cache ^
--Classpath="%JARS%"
EXIT /B

:uninstall

ECHO Uninstalling service Prudence${service}...

%PRUNSRV% ^
//DS//PrudenceJavaScript
EXIT /B

:start

NET START Prudence${service}
EXIT /B

:stop

NET STOP Prudence${service}
EXIT /B

:monitor

START "Prudence${service} Monitor" %PRUNMGR% ^
//MS//Prudence${service}"
EXIT /B

:settings

START "Prudence${service} Settings" %PRUNMGR% ^
//ES//Prudence${service}
EXIT /B
