package com.meetup.kafkalondon.kafka;

import com.meetup.kafkalondon.Receiver;
import com.meetup.kafkalondon.metrics.MetricCalibrator;
import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.KEY;
import static pl.tlinkowski.unij.api.UniLists.of;

@Slf4j
public class KafkaReceiver extends Receiver {
    private final Consumer<String, byte[]> consumer;
    private final MetricCalibrator metricCalibrator = new MetricCalibrator();
    private final ParallelStreamProcessor<String, byte[]> parallelConsumer;

    public KafkaReceiver() throws Exception {

        Properties consumerProperties = new Properties();
        sourceProperties.forEach((key, value) -> consumerProperties.put(key, value));
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        final String groupId = String.format("kafkalondon.meetup.%s", this.getClass().getSimpleName().toLowerCase());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumer = new KafkaConsumer<String, byte[]>(consumerProperties);

        ParallelConsumerOptions options = ParallelConsumerOptions.<String, byte[]>builder()
                .ordering(KEY)
                .maxConcurrency(config.parallelConsumerMaxConcurrency)
                .consumer(consumer)
                .build();

        parallelConsumer = ParallelStreamProcessor.createEosStreamProcessor(options);
        parallelConsumer.subscribe(of(config.inboundTopic));
    }

    public void emitMetrics() throws Exception {
        metricCalibrator.emitMetrics();
        shutdown();
    }

    @Override
    public void shutdown() throws Exception {
        parallelConsumer.close();
        consumer.close();
    }

    public void receive() {
        parallelConsumer.poll(record -> {
            final long currentTimeNanos = Instant.now().getNano();
            metricCalibrator.incrementPacketsReceivedCounter();
            String event = new String(record.value());
            JSONObject jsonData = new JSONObject(event);
            long endToEndLatencyMicros =
                    TimeUnit.NANOSECONDS.toMicros(currentTimeNanos - jsonData.getLong("publish-time"));
            log.info("Event received : {}", event);
            log.info("Latencies in Microseconds = EndToEndLatency: {}", endToEndLatencyMicros);
            try {
                metricCalibrator.registerAndRecordLatency(endToEndLatencyMicros);
            } catch (Exception e) {}
        });
    }

    @Override
    public void start() {
        receive();
    }
}
