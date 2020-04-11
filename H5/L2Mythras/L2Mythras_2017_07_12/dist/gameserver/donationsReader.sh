#!/bin/bash
echo "Starting Donations Script"
java -cp ../libs/DonationsReader1.0.0.jar main.Main > donationsLog.txt 2>&1 &
