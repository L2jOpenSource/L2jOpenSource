#!/bin/sh
java -Djava.util.logging.config.file=config/console.cfg -cp ./libs/*:l2jserver.jar:mysql-connector-java-8.0.15.jar net.sf.l2j.accountmanager.SQLAccountManager
