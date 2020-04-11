@echo off
title L2JFrozen 1.5 Game Server Console
color 0B
:start

REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF8 -server -Xms1024m -Xmx1024m -XX:MetaspaceSize=256M -XX:+UseConcMarkSweepGC -XX:+UseStringDeduplication -cp ./lib/*;l2jfrozen-core.jar com.l2jfrozen.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------

if ERRORLEVEL 5 goto taskrestart
if ERRORLEVEL 4 goto taskdown
REM 3 - abort
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:taskrestart
echo.
echo Auto Task Restart ...
echo.
goto start

:taskdown
echo .
echo Server terminated (Auto task)
echo .

:restart
echo.
echo Admin Restart ...
echo.
goto start

:end
echo.
echo server terminated
echo.
exit