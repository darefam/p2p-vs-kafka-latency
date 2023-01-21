# p2p-vs-kafka-e2e-latency

## How to Run in Docker

1. Build the docker images

```bash
$ ./gradlew :event-publisher:dockerBuildImage
$ ./gradlew :event-receiver:dockerBuildImage

```

2. Start pipeline using
```bash
$  bin/start.sh -m <channel_mode> -k <kafka_cluster> -a <producer_type> -c <consumer_concurrency> -o <topic_config_override> -p <partition_count> -r <replication_factor> -f <fsync>

```
By default, the test runs for 120s at the rate of 5 messages per second. The duration and load can be configured prior to each run in the `event-publisher` configuration file.

Argument | Name                    | Description                                                                                                                         | Valid Options                                              
--- |-------------------------|-------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------
**-m** | `channel_mode`          | The channel mode of communication between the services                                                                              | `tcp` `kafka`                                              
**-k** | `kafka_cluster`         | The Kafka cluster type if using Kafka channel mode                                                                                  | `multi-broker` `single-broker` `ssl-broker` `kraft-broker` 
**-a** | `producer_type`         | Message durability guarantee of the embedded Kafka producer used in streaming pipes if using Kafka channel mode                     | `nack` `ackLeader` `ackAll` `idempotent`                   
**-c** | `consumer_concurrency`  | Set 'consumer_concurrency' as high as possible, much higher than CPU core count for extreme parallelism if using Kafka channel mode | `Integer`                                                  
**-o** | `topic_config_override` | Topic configuration overrides if using Kafka channel mode                                                                           | `key=val` comma-separated list                             
**-p** | `partition_count`       | The number of topic partitions if using Kafka channel mode                                                                          | `Integer`                                                  
**-r** | `replication_factor`    | The topic replication factor if using Kafka channel mode                                                                            | `Integer`                                                  
**-f** | `fsync`                 | Whether or not to fsync i.e. flush every message to disk if using Kafka channel mode                                                |  `Yes` or `No`

To run in TCP channel mode, execute

```bash
$  bin/start.sh -m tcp

```

Other possible Kafka channel executions

Test Plan | Command Script 
--- | --- 
**Single Broker Cluster/At Most Once Delivery/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k single-broker -a nack -p 16 -r 1 -c 100 -f No`
**Single Broker Cluster/At Least Once Delivery/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k single-broker -a ackLeader -p 16 -r 1 -c 100 -f No`
**Single Broker Cluster/Strongest Durability Guarantee/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k single-broker -a ackAll -p 16 -r 1 -c 100 -f No`
**Single Broker Cluster/Idempotent Delivery/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k single-broker -a idempotent -p 16 -r 1 -c 100 -f No`
**Multi Broker Cluster/At Most Once Delivery/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k multi-broker -a nack -p 16 -r 3 -c 100 -f No`
**Multi Broker Cluster/At Least Once Delivery/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k multi-broker -a ackLeader -p 16 -r 3 -c 100 -f No`
**Multi Broker Cluster/Strongest Durability Guarantee/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k multi-broker -a ackAll -p 16 -r 3 -c 100 -f No`
**Multi Broker Cluster/Idempotent Delivery/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k multi-broker -a idempotent -p 16 -r 3 -c 100 -f No`
**SSL Broker Cluster/At Most Once Delivery/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k ssl-broker -a nack -p 16 -r 3 -c 100 -f No`
**SSL Broker Cluster/At Least Once Delivery/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k ssl-broker -a ackLeader -p 16 -r 3 -c 100 -f No`
**SSL Broker Cluster/Strongest Durability Guarantee/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k ssl-broker -a ackAll -p 16 -r 3 -c 100 -f No`
**SSL Broker Cluster/Idempotent Delivery/16 Partitions/RF=3/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k ssl-broker -a idempotent -p 16 -r 3 -c 100 -f No`
**KRaft Broker Cluster/At Most Once Delivery/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k kraft-broker -a nack -p 16 -r 1 -c 100 -f No`
**KRaft Broker Cluster/At Least Once Delivery/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k kraft-broker -a ackLeader -p 16 -r 1 -c 100 -f No`
**KRaft Broker Cluster/Strongest Durability Guarantee/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k kraft-broker -a ackAll -p 16 -r 1 -c 100 -f No`
**KRaft Broker Cluster/Idempotent Delivery/16 Partitions/RF=1/Consumer_Concurrency=100/Fsync=No** | `bin/start.sh -m kafka -k kraft-broker -a idempotent -p 16 -r 1 -c 100 -f No`

3. To stop all container services and shutdown the docker environment, run:
```bash
$  bin/stop.sh

```

## How to run it locally outside docker

1. Start a standalone Kafka cluster. Only required for Kafka channel mode:

```bash
$ bin/start-standalone.sh 
```

2. Start the services using appropriate startup scripts and passing the relevant channel mode. For example, for Kafka channel mode, execute:

```bash
$ bin/event-receiver.sh kafka
$ bin/event-publisher.sh kafka

```

## Visualizing the E2E latency

Browse to the prometheus exporter endpoint exposed by the `event-receiver`
`http://localhost:9292/metrics`

or browse the prometheus agent which has a `event-receiver` target already configured via

`http://localhost:9090`

The latencies are computed as 50th, 75th, 95th, 99th 99.9th, 99.99th `percentiles` and `max`

Tail `event-receiver` log to also see the E2E latencies.



