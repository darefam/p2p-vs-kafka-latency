package com.meetup.kafkalondon.kafka;

import com.meetup.kafkalondon.Publisher;
import com.meetup.kafkalondon.event.LoadGenerator;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class KafkaPublisher extends Publisher {

    private final ExecutorService executor = Executors.newCachedThreadPool(new DefaultThreadFactory("kafka-publisher"));
    private final LoadGenerator loadGenerator;
    private final KafkaProducer<String, byte[]> producer;

    public KafkaPublisher() throws Exception {
        Properties producerProperties = new Properties();
        sinkProperties.forEach((key, value) -> producerProperties.put(key, value));
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        producer = new KafkaProducer<>(producerProperties);
        loadGenerator = new LoadGenerator(this, config.load, config.duration);
    }

    public CompletableFuture<Void> produceAsync(Optional<String> key, byte[] data) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(config.outboundTopic, key.orElse(null), data);

        CompletableFuture<Void> future = new CompletableFuture<>();

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                future.completeExceptionally(exception);
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    @Override
    public void send(String event) {
        executor.submit(() -> {
            produceAsync(Optional.ofNullable(String.valueOf(LoadGenerator.RANDOM.nextInt(LoadGenerator.KEY_RANGE))),
                         event.getBytes(StandardCharsets.UTF_8)).thenRun(() -> {
            }).exceptionally(ex -> {
                log.warn("Write error on message", ex);
                return null;
            });
        });
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
        producer.close();
    }

    @Override
    public void start() throws InterruptedException {
        loadGenerator.generateAndSend();
    }
}
