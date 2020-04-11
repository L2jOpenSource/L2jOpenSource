#!/bin/bash

./LoginServer_loop.sh & # run Lineage 2 server and keep process alive when terminal is closed
sleep 1 # Wait 1 second
tail -f log/stdout.log # Show log like windows, use "Ctrl + C" to cancel