package com.meetup.kafkalondon.metrics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;
import org.apache.bookkeeper.stats.Counter;
import org.apache.bookkeeper.stats.OpStatsLogger;
import org.apache.bookkeeper.stats.Stats;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.bookkeeper.stats.StatsProvider;
import org.apache.bookkeeper.stats.prometheus.PrometheusMetricsProvider;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class MetricCalibrator {
    private static final DecimalFormat dec = new PaddingDecimalFormat("0.000", 4);
    private static final DecimalFormat throughputFormat = new PaddingDecimalFormat("0.000", 4);
    private static final int prometheusPort = 9292;
    private final Recorder endToEndLatencyRecorder = new Recorder(TimeUnit.HOURS.toMicros(12), 5);
    private final OpStatsLogger endToEndLatencyStats;
    private final Counter packetsReceivedCounter;
    private final StatsProvider provider;

    public MetricCalibrator() {
        Configuration conf = new CompositeConfiguration();
        conf.setProperty(Stats.STATS_PROVIDER_CLASS, PrometheusMetricsProvider.class.getName());
        conf.setProperty("prometheusStatsHttpPort", prometheusPort);
        Stats.loadStatsProvider(conf);
        provider = Stats.get();
        provider.start(conf);

        StatsLogger statsLogger = provider.getStatsLogger("kafka-meetup-pipeline");
        StatsLogger consumerStatsLogger = statsLogger.scope("tcp-vs-kafka");
        packetsReceivedCounter = consumerStatsLogger.getCounter("packets_recv");
        endToEndLatencyStats = consumerStatsLogger.getOpStatsLogger("e2e_latency");
    }

    public void emitMetrics() {
        final Histogram endToEndLatency = endToEndLatencyRecorder.getIntervalHistogram();

        log.info("E2E Latency (ms) avg: {} - 50%: {} - 99%: {} - 99.9%: {} - Max: {}",
                 dec.format(microsToMillis(endToEndLatency.getMean())),
                 dec.format(microsToMillis(endToEndLatency.getValueAtPercentile(50))),
                 dec.format(microsToMillis(endToEndLatency.getValueAtPercentile(99))),
                 dec.format(microsToMillis(endToEndLatency.getValueAtPercentile(99.9))),
                 throughputFormat.format(microsToMillis(endToEndLatency.getMaxValue())));

        log.info("{},{},{},{},{}",
                 dec.format(microsToMillis(endToEndLatency.getMean())),
                 dec.format(microsToMillis(endToEndLatency.getValueAtPercentile(50))),
                 dec.format(microsToMillis(endToEndLatency.getValueAtPercentile(99))),
                 dec.format(microsToMillis(endToEndLatency.getValueAtPercentile(99.9))),
                 throughputFormat.format(microsToMillis(endToEndLatency.getMaxValue())));
        provider.stop();
    }

    public void incrementPacketsReceivedCounter() {
        packetsReceivedCounter.inc();
    }

    public void registerAndRecordLatency(long endToEndLatencyMicros) {
        endToEndLatencyStats.registerSuccessfulEvent(endToEndLatencyMicros, TimeUnit.MICROSECONDS);
        endToEndLatencyRecorder.recordValue(endToEndLatencyMicros);
    }

    private static double microsToMillis(double microTime) {
        return microTime / (1000);
    }
}
