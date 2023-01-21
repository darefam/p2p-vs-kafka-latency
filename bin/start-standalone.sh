#!/bin/bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

. "${DIR}/util-functions.sh"
. "${DIR}/common-functions.sh"
. "${DIR}/broker-overrides.sh"

# Stop existing Docker containers
${DIR}/stop.sh

compose_file=${DIR}/../docker/standalone/docker-compose.yml

docker-compose -f $compose_file up --no-recreate -d

MAX_WAIT=150
retry $MAX_WAIT host_check_up kafka || exit 1

echo "Kafka cluster is up and running"
echo "Initializing Kafka cluster....."
sleep 60

# Let's create the application topic
setup_kafka="kafka"
partition_count=16
replication_factor=1
min_insync_replicas=1

echo -e "Creating topic, kfk-local.meetup-events"
createTopic "kfk-local.meetup-events"