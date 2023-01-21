package com.meetup.kafkalondon.tcp;

import com.meetup.kafkalondon.Publisher;
import com.meetup.kafkalondon.event.LoadGenerator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;

public class TcpPublisher extends Publisher {

    protected final String connectorHost;
    protected final int connectorPort;
    final ChannelFuture channelFuture;
    final TcpConnector tcpConnector;
    private final LoadGenerator loadGenerator;

    public TcpPublisher() throws Exception {
        connectorPort = Integer.parseInt(sinkProperties.getProperty("connector.port"));
        connectorHost = sinkProperties.getProperty("connector.host");
        tcpConnector = new TcpConnector(connectorHost, connectorPort);
        channelFuture = tcpConnector.startup();
        loadGenerator = new LoadGenerator(this, config.load, config.duration);
    }

    @Override
    public void send(String event) {
        if (channelFuture.isSuccess()) {
            channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(event.getBytes()));
        }
    }

    @Override
    public void shutdown() throws Exception {
        tcpConnector.shutdown();
    }

    @Override
    public void start() throws InterruptedException {
        loadGenerator.generateAndSend();
    }
}
