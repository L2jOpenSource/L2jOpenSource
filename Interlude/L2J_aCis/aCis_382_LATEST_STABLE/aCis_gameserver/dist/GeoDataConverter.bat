@echo off
title aCis geodata converter

java -Xmx512m -cp ./libs/*; net.sf.l2j.geodataconverter.GeoDataConverter

pause
