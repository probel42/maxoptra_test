#!/bin/sh

while ((i++)); read -r line;
do
	[ -z "$line" ] && continue
	message="$(echo $line | xmlstarlet ed -r "/row" -v GPSPosition)"
	echo [ $i ] $message
	curl -X POST -H "Content-Type: application/xml" -d "$message" http://localhost:8080/app/gps
done < <(xmlstarlet sel -t -c '/data/node()' -n actual_gps_positions.xml)
