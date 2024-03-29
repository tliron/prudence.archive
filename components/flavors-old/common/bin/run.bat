@ECHO OFF
REM #
REM # Copyright 2009-2011 Three Crickets LLC.
REM #
REM # The contents of this file are subject to the terms of the LGPL version 3.0:
REM # http://www.opensource.org/licenses/lgpl-3.0.html
REM #
REM # Alternatively, you can obtain a royalty free commercial license with less
REM # limitations, transferable or non-transferable, directly from Three Crickets
REM # at http://threecrickets.com/
REM #

SET ORIGINAL="%CD%"
CD /D "%0%\..\.."

SETLOCAL ENABLEDELAYEDEXPANSION
SET JARS=
FOR /F %%J IN ('DIR /B libraries\*.jar') DO SET JARS=!JARS!;%CD%\libraries\%%~J
SET JARS=%JARS:~1%

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
ECHO Usage: run.bat {console^|start^|stop^|install^|uninstall^|monitor^|settings}
ECHO.
ECHO If you are unsure which option to use, "console" mode is recommended.
ECHO.
ECHO For information on setting up Prudence to start and stop as a daemon, see:
ECHO http://threecrickets.com/prudence/manual/daemon/ 
CD /D "%ORIGINAL%"
EXIT /B

:console

"%JAVA%" ^
-cp "%JARS%" ^
-Dscripturian.cache=cache ^
-Dhazelcast.config=configuration\hazelcast.conf ^
#if(($distribution == 'python') || ($distribution == 'kitchensink'))
-Dpython.home=common\libraries ^
-Dpython.verbose=warning ^
#end
-Djava.util.logging.config.file=none ^
-Dnet.spy.log.LoggerImpl=net.spy.log.SunLogger ^
com.threecrickets.scripturian.Scripturian instance
CD /D "%ORIGINAL%"
EXIT /B

:install

ECHO Installing service Prudence${service}... 

%PRUNSRV% ^
//IS//Prudence${service} ^
--DisplayName="Prudence for ${service}" ^
--Description="Prudence for ${service}" ^
--LogPath="%CD%\logs" ^
--LogPrefix=service ^
--LogLevel=Debug ^
--LogJniMessages=1 ^
--PidFile="%CD%\run.pid" ^
--StdOutput=auto ^
--StdError=auto ^
--StartMode=jvm ^
--StartClass=com.threecrickets.prudence.PrudenceDaemon ^
--StartParams=--base-path="%CD%";instance ^
--StopMode=jvm ^
--StopClass=com.threecrickets.prudence.PrudenceDaemon ^
--StopMethod=stop ^
--Jvm=auto ^
--JvmOptions=-Dscripturian.cache="%CD%\cache" ^
++JvmOptions=-Dhazelcast.config="%CD%\configuration\hazelcast.conf" ^
#if(($distribution == 'python') || ($distribution == 'kitchensink'))
++JvmOptions=-Dpython.home="%CD%\common\libraries" ^
++JvmOptions=-Dpython.verbose=warning ^
#end
++JvmOptions=-Djava.util.logging.config.file=none ^
++JvmOptions=-Dnet.spy.log.LoggerImpl=net.spy.log.SunLogger ^
--Classpath="%JARS%"
CD /D "%ORIGINAL%"
EXIT /B

:uninstall

ECHO Uninstalling service Prudence${service}...

%PRUNSRV% ^
//DS//Prudence${service}
CD /D "%ORIGINAL%"
EXIT /B

:start

NET START Prudence${service}
CD /D "%ORIGINAL%"
EXIT /B

:stop

NET STOP Prudence${service}
CD /D "%ORIGINAL%"
EXIT /B

:monitor

START "Prudence${service} Monitor" %PRUNMGR% ^
//MS//Prudence${service}"
CD /D "%ORIGINAL%"
EXIT /B

:settings

START "Prudence${service} Settings" %PRUNMGR% ^
//ES//Prudence${service}
CD /D "%ORIGINAL%"
EXIT /B
