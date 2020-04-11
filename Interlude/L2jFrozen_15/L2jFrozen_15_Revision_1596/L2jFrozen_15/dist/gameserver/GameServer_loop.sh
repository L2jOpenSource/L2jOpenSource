#!/bin/bash

err=1
until [ $err == 0 ];
do
	exec -a l2gameserver java -Dfile.encoding=UTF8 -server -Xms1024m -Xmx1024m -XX:MetaspaceSize=256M -XX:+UseConcMarkSweepGC -XX:+UseStringDeduplication -cp lib/*:l2jfrozen-core.jar com.l2jfrozen.gameserver.GameServer > log/stdout.log 2>&1
	err=$?
	sleep 10
done
