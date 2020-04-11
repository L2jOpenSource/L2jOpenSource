@echo off
COLOR 0b
title L2Mythras: Game Server Console
:start
echo Starting L2Mythras GameServer.
echo.

set JAVA_OPTS=%JAVA_OPTS% -XX:PermSize=128m
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=256m

set JAVA_OPTS=%JAVA_OPTS% -Xmn512m
set JAVA_OPTS=%JAVA_OPTS% -Xms2096m
set JAVA_OPTS=%JAVA_OPTS% -Xmx2096m

set JAVA_OPTS=%JAVA_OPTS% -Xnoclassgc
set JAVA_OPTS=%JAVA_OPTS% -XX:+AggressiveOpts
set JAVA_OPTS=%JAVA_OPTS% -XX:TargetSurvivorRatio=90
set JAVA_OPTS=%JAVA_OPTS% -XX:SurvivorRatio=16
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxTenuringThreshold=12
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParNewGC
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseConcMarkSweepGC
set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSIncrementalMode
set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSIncrementalPacing
set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSParallelRemarkEnabled

set JAVA_OPTS=%JAVA_OPTS% -XX:UseSSE=3
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseFastAccessorMethods

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 -server -Dfile.encoding=UTF-8 -Xmx9G -XX:PermSize=2G -XX:+UseConcMarkSweepGC -XX:+UseTLAB -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:./gc.log -cp config;./lameguard-1.9.5.jar;./libs/* com.lameguard.LameGuard l2f.gameserver.GameServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Server restarted ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly ...
echo.
:end
echo.
echo Server terminated ...
echo.

pause
