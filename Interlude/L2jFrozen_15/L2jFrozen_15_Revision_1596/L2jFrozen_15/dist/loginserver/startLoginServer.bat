@echo off
title L2JFrozen 1.5 Login Server Console
color 0B
:start

java -Dfile.encoding=UTF8 -Xms128m -Xmx128m -cp ./lib/*;l2jfrozen-core.jar com.l2jfrozen.loginserver.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restarted ...
ping -n 5 localhost > nul
echo.
goto start
:error
echo.
echo LoginServer terminated abnormaly
ping -n 5 localhost > nul
echo.
goto question
:end
echo.
echo LoginServer terminated
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
