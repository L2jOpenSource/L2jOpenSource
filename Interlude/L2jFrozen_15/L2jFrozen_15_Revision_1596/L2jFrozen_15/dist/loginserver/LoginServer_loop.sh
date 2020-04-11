#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	exec -a l2loginserver java -Xms128m -Xmx128m -cp lib/*:l2jfrozen-core.jar com.l2jfrozen.loginserver.L2LoginServer > log/stdout.log 2>&1
	err=$?
	sleep 10;
done
