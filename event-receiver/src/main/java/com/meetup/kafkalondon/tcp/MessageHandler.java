package com.meetup.kafkalondon.tcp;

import com.meetup.kafkalondon.metrics.MetricCalibrator;
import com.meetup.kafkalondon.tcp.entity.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    private final MetricCalibrator metricCalibrator = new MetricCalibrator();

    public void emitMetrics() {
        metricCalibrator.emitMetrics();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error(cause.getMessage());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        handleMessage(ctx, msg);
    }

    private void handleMessage(ChannelHandlerContext ctx, Message msg) {
        final long currentTimeNanos = Instant.now().getNano();
        metricCalibrator.incrementPacketsReceivedCounter();
        JSONObject jsonData = new JSONObject(msg.getMessage());
        long endToEndLatencyMicros = TimeUnit.NANOSECONDS.toMicros(currentTimeNanos - jsonData.getLong("publish-time"));
        log.info("Event received : {}", msg.getMessage());
        log.info("Latencies in Microseconds = EndToEndLatency: {}", endToEndLatencyMicros);
        metricCalibrator.registerAndRecordLatency(endToEndLatencyMicros);
    }

    public interface WriteListener {
        void messageRespond(boolean success);
    }
}
