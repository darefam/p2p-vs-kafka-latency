#!/bin/bash

set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

. "${DIR}/common-functions.sh"
. "${DIR}/util-functions.sh"

checkMode $1
validateOption $1 "$VALID_CHANNEL_MODES"
channel_mode=$1
echo "channel_mode=$channel_mode"

JVM_MEM="-Xms512m -Xmx512m -XX:+UseG1GC"
export CHANNEL_MODE=$channel_mode
if [[ $channel_mode = "kafka" ]]
then
  export PRODUCER_TYPE=nack
fi
export LOG_LEVEL=INFO
gradle :event-publisher:buildJar && \
java $JVM_MEM -jar event-publisher/build/libs/event-publisher-1.0.jar