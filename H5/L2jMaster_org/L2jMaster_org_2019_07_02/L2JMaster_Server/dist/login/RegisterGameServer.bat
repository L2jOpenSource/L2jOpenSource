@echo off
title L2JMaster - Register Game Server
color 17
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;login.jar com.l2jserver.tools.gsregistering.BaseGameServerRegister -c
pause