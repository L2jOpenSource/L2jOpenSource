#!/bin/bash

pkill -f l2gameserver # Linux command to send SIGTERM to process by name
tail -f log/stdout.log # Show log like windows, use "Ctrl + C" to cancel