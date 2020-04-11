@echo off
title L2J - Register Game Server
color 17
java -Djava.util.logging.config.file=console.cfg -cp config/xml;./../libs/*;login.jar l2r.tools.gsregistering.BaseGameServerRegister -c
pause