#!/bin/sh

USER=root
PASS=
DBNAME=frozen
DBHOST=127.0.0.1

mysql --host=$DBHOST --user=$USER --password=$PASS -e "CREATE DATABASE IF NOT EXISTS $DBNAME;"

for sqlfile in install/*.sql
do
        echo Installing table $sqlfile to " $DBNAME " ...
        mysql --host=$DBHOST --user=$USER --password=$PASS $DBNAME < $sqlfile
done
