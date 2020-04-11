#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -Xbootclasspath/p:./../libs/l2ft.jar -cp config/xml:./../libs/*:login.jar l2r.tools.accountmanager.SQLAccountManager
