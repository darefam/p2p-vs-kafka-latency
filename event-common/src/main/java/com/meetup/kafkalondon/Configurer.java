package com.meetup.kafkalondon;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

@Slf4j
public abstract class Configurer {

    protected static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    final protected Config config;
    final protected Properties sinkProperties;
    final protected Properties sourceProperties;

    public Configurer() throws Exception {
        final String channelMode = CommonUtil.getChannelMode();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(String.format("application-%s.yaml", channelMode));

        config = mapper.readValue(inputStream, Config.class);

        final String inboundConfig, outboundConfig;
        if ((inboundConfig = System.getenv().get("INBOUND_CONFIG")) != null)
            config.inboundConfig = inboundConfig;

        if ((outboundConfig = System.getenv().get("OUTBOUND_CONFIG")) != null)
            config.outboundConfig = outboundConfig;

        if (channelMode.equals(ChannelMode.kafka.name())) {

            final String parallelConsumerMaxConcurrency;
            if ((parallelConsumerMaxConcurrency = System.getenv().get("PARALLEL_CONSUMER_MAX_CONCURRENCY")) != null)
                config.parallelConsumerMaxConcurrency = Integer.parseInt(parallelConsumerMaxConcurrency);

            final String brokerConfig, producerType, producerConfig, consumerConfig, inboundTopic, outboundTopic;
            if ((brokerConfig = System.getenv().get("BROKER_CONFIG")) != null)
                config.brokerConfig = brokerConfig;

            if (((producerType = System.getenv().get("PRODUCER_TYPE")) != null)
                && (ProducerType.isValidProducerType(producerType))) {
                if (producerType.equals(ProducerType.nack.name()) && config.nack != null) {
                    config.outboundConfig = config.nack;
                } else if (producerType.equals(ProducerType.ackLeader.name()) && config.ackLeader != null) {
                    config.outboundConfig = config.ackLeader;
                } else if (producerType.equals(ProducerType.ackAll.name()) && config.ackAll != null) {
                    config.outboundConfig = config.ackAll;
                } else if (producerType.equals(ProducerType.idempotent.name()) && config.idempotent != null) {
                    config.outboundConfig = config.idempotent;
                } else {
                    log.warn(
                            "No matching PRODUCER_TYPE configuration found. This pipe will silently ignore it and may" +
                            " not need it!");
                }
            } else {
                log.warn(
                        "Either an invalid or no PRODUCER_TYPE specified. Pls check but the service will proceed with" +
                        " the OUTBOUND_CONFIG!");
            }

            if ((outboundTopic = System.getenv().get("OUTBOUND_TOPIC_NAME")) != null)
                config.outboundTopic = outboundTopic;

            if ((inboundTopic = System.getenv().get("INBOUND_TOPIC_NAME")) != null)
                config.inboundTopic = inboundTopic;

            sourceProperties = new Properties();
            sourceProperties.load(new StringReader(config.brokerConfig));
            if (config.inboundConfig != null)
                sourceProperties.load(new StringReader(config.inboundConfig));
            if ((consumerConfig = System.getenv().get("CONSUMER_CONFIG")) != null)
                sourceProperties.load(new StringReader(consumerConfig));

            sinkProperties = new Properties();
            sinkProperties.load(new StringReader(config.brokerConfig));
            if (config.outboundConfig != null)
                sinkProperties.load(new StringReader(config.outboundConfig));
            if ((producerConfig = System.getenv().get("PRODUCER_CONFIG")) != null)
                sinkProperties.load(new StringReader(producerConfig));
        } else {
            sourceProperties = new Properties();
            if (config.inboundConfig != null)
                sourceProperties.load(new StringReader(config.inboundConfig));

            sinkProperties = new Properties();
            if (config.outboundConfig != null)
                sinkProperties.load(new StringReader(config.outboundConfig));
        }

        log.info("source-config: {}, sink-config: {}", sourceProperties, sinkProperties);
    }

    public abstract void start() throws IOException, InterruptedException;
}
