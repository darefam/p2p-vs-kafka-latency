#!/bin/bash

while getopts "m:k:a:c:o:p:r:f:" flag
do
   case "$flag" in
      m ) channel_mode="$OPTARG" ;;
      k ) kafka_cluster="$OPTARG" ;;
      a ) producer_type="$OPTARG" ;;
      c ) consumer_concurrency="$OPTARG" ;;
      o ) topic_config_override="$OPTARG" ;;
      p ) partition_count="$OPTARG" ;;
      r ) replication_factor="$OPTARG" ;;
      f ) fsync="$OPTARG" ;;
      ? ) helpFunction ;;
   esac
done

checkMode $channel_mode
validateOption $channel_mode "$VALID_CHANNEL_MODES"
echo "channel_mode=$channel_mode"

if [[ $channel_mode = "kafka" ]]
then
    if [ -z "$kafka_cluster" ] || [ -z "$producer_type" ]
    then
      echo "Mandatory parameters, '-k' and '-p' for Kafka channel mode are missing";
      helpFunction
    else
      validateOption $kafka_cluster "$VALID_KAFKA_CLUSTER_SIZES"
      validateOption $producer_type "$VALID_PRODUCER_TYPES"
    fi

    if [[ $kafka_cluster = "multi-broker" ]] || [[ $kafka_cluster = "ssl-broker" ]]
    then
      kafka_hostnames="kafka-1 kafka-2 kafka-3"
      # Set the broker host for creating the topics
      setup_kafka="kafka-2"
      if [[ $replication_factor -ne 3 ]]
      then
        echo "Replication factor of ${replication_factor} is not acceptable for a cluster of 3 brokers. Sstting it to 3..."
        replication_factor=3
      fi
      min_insync_replicas=2
    fi

    if [[ $kafka_cluster = "ssl-broker" ]]
    then
      createSSLCertificates
      command_config=" --command-config /etc/kafka/secrets/admin-client.properties"
      export SSL_PASSWORD=meetupsecret
    fi

    if [[ $kafka_cluster = "single-broker" ]] || [[ $kafka_cluster = "kraft-broker" ]]
    then
      replication_factor=1
      min_insync_replicas=1
      # Set the broker host for creating the topics
      setup_kafka="kafka"
    fi

    if [ -z "$consumer_concurrency" ]
    then
       # Default to 100
       consumer_concurrency=100
    fi

    if [ -z "$partition_count" ]
    then
       # Default to 16
       partition_count=16
    fi

    if [ -z ${topic_config_override+x} ]; then
      echo "No topic configuration overrides";
    else
      echo "topic_config_override: [$topic_config_override]"
      IFS=',' read -ra ADDR <<< "$topic_config_override"
      for i in "${ADDR[@]}"; do
        extra_config+=" --config $i"
      done
    fi

    if [ -z ${fsync+x} ]; then
      echo "fsync will not be performed on the broker(s)";
    else
      if [[ "$fsync" =~ ^([yY][eE][sS]|[yY])$ ]]
      then
        echo "Enabling fsync: Every message published will be flushed to disk"
        extra_config+=" --config flush.messages=1 --config flush.ms=0"
      fi
    fi

    echo "topic configuration overrides=$extra_config"
    echo "kafka_cluster=$kafka_cluster"
    echo "kafka_hostnames=$kafka_hostnames"
    echo "setup_kafka=$setup_kafka"
    echo "producer_type=$producer_type"
    echo "replication_factor=$replication_factor"
    echo "partition_count=$partition_count"
    echo "min_insync_replicas=$min_insync_replicas"
    echo "consumer_concurrency=$consumer_concurrency"

    # Export some parameters as environment variables so that they are available in docker-compose
    export KAFKA_CLUSTER=$kafka_cluster
    export PRODUCER_TYPE=$producer_type
    export CONSUMER_CONCURRENCY=$consumer_concurrency
fi
