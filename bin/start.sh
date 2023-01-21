#!/bin/bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

. "${DIR}/util-functions.sh"
. "${DIR}/common-functions.sh"
. "${DIR}/init.sh"
. "${DIR}/broker-overrides.sh"

export CHANNEL_MODE=$channel_mode
export DIR=${DIR}

# Stop existing Docker containers
${DIR}/stop.sh

MAX_WAIT=150
if [[ $channel_mode = "kafka" ]]
then
  # Bring up Zookeeper and kafka cluster using the right docker-compose file
  compose_file=${DIR}/../docker/all-in-one/$kafka_cluster/docker-compose.yml
  echo "Setting up a local Kafka environment"
  if [[ $kafka_cluster = "multi-broker" ]] || [[ $kafka_cluster = "ssl-broker" ]]
  then
    docker-compose -f "$compose_file" up --no-recreate -d zookeeper ${kafka_hostnames}
    for host in $kafka_hostnames; do
        retry $MAX_WAIT host_check_up ${host} || exit 1
    done
  else
    if [[ $kafka_cluster = "kraft-broker" ]]
    then
      # Doesn't require Zookeeper
      docker-compose -f $compose_file up --no-recreate -d kafka
    else
      docker-compose -f $compose_file up --no-recreate -d zookeeper kafka
    fi
    retry $MAX_WAIT host_check_up kafka || exit 1
  fi

  echo "Kafka cluster is up and running"
  echo "Initializing Kafka cluster.."
  sleep 10

  # Let's create the application topic
  echo -e "Creating topic, kfk-local.meetup-events"
  createTopic "kfk-local.meetup-events"

else
  compose_file=${DIR}/../docker/all-in-one/no-broker/docker-compose.yml
fi

# Bring up other containers - event-receiver-service, event-publisher and prometheus
docker-compose -f $compose_file up --no-recreate -d event-receiver event-publisher prometheus