#!/bin/bash

docker ps -aq | xargs docker stop
docker volume prune -f
docker container prune -f