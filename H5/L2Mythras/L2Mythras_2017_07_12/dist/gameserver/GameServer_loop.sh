#!/bin/bash

while :;
do
java -Xbootclasspath/p:./jsr167.jar -server -Dfile.encoding=UTF-8 -Duser.timezone="Europe/Paris" -Xmx16G -cp ./lib/smrt.jar:./lib/smrt-core.jar:config:./libs/* ru.akumu.smartguard.SmartGuard l2f.gameserver.GameServer > log/stdout.log 2>&1
        [ $? -ne 2 ] && break
        sleep 30;
done

