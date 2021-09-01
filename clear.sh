#!/bin/sh

docker rmi ibelan_maxoptra_test_gps_app
# docker rmi ibelan_maxoptra_test_gps_app_build

docker volume rm ibelan_maxoptra_test_gps_app_build_volume
docker volume rm ibelan_maxoptra_test_postgres_volume

# docker volume rm $(docker volume ls -q)