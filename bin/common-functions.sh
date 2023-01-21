#!/bin/bash

VALID_CHANNEL_MODES="tcp kafka"
VALID_KAFKA_CLUSTER_SIZES="multi-broker single-broker ssl-broker kraft-broker"
VALID_PRODUCER_TYPES="nack ackLeader ackAll idempotent"
# Cipher suites across all services
export SSL_CIPHER_SUITES=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256

host_check_up()
{
  containerName=$1
  if [[ $(docker inspect --format '{{json .State.Health.Status }}' $containerName) == "\"healthy\"" ]]; then
    return 0
  fi
  return 1
}

helpFunction()
{
   echo ""
   echo "Usage: $0 -m channel_mode -k kafka_cluster -a producer_type -c consumer_concurrency -o topic_config_override -p partition_count -r replication_factor -f fsync"
   echo -e "\t-m Please specify either 'tcp' or 'kafka' as channel_mode."
   echo -e "\t-k Please specify either 'single-broker', 'multi-broker', 'ssl-broker' or 'kraft-broker' as kafka cluster size if using Kafka channel mode. No need to specify this parameter for TCP channel mode."
   echo -e "\t-a Please specify either 'nack', 'ackLeader', 'ackAll' or 'idempotent' as producer_type if using Kafka channel mode"
   echo -e "\t-c Set 'consumer_concurrency' quite high, much higher than CPU core count if using Kafka channel mode"
   echo -e "\t-o Please specify topic configuration overrides as 'key=val' comma-separated list if using Kafka channel mode"
   echo -e "\t-p Please specify number of topic partitions if using Kafka channel mode"
   echo -e "\t-r Please specify the replication factor if using Kafka channel mode"
   echo -e "\t-f Please specify whether or not to fsync i.e. flush every message to disk if using Kafka channel mode. Valid options are 'Yes' or 'No'"
   exit 1
}

createTopic(){
  local topic_name="$1"
  docker-compose -f $compose_file exec ${setup_kafka} kafka-topics --create \
      --bootstrap-server ${setup_kafka}:9092 \
      --topic ${topic_name} \
      --partitions ${partition_count} \
      --replication-factor ${replication_factor} \
      --config min.insync.replicas=${min_insync_replicas} \
      ${extra_config} \
      ${command_config}
}

createSSLCertificates(){
  currentDir=`pwd`
  cd ${DIR}/secrets
  ${DIR}/secrets/create-certs.sh
  cd ${currentDir}
}

