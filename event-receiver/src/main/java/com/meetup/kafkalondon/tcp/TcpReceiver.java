package com.meetup.kafkalondon.tcp;

import com.meetup.kafkalondon.Receiver;

public class TcpReceiver extends Receiver {

    protected final String acceptorHost;
    protected final int acceptorPort;
    final TcpAcceptor tcpAcceptor;

    public TcpReceiver() throws Exception {
        acceptorPort = Integer.parseInt(sourceProperties.getProperty("acceptor.port"));
        acceptorHost = sourceProperties.getProperty("acceptor.host");
        tcpAcceptor = new TcpAcceptor(MessagePipelineFactory.class);
    }

    public void emitMetrics() throws Exception {
        shutdown();
    }

    @Override
    public void shutdown() throws Exception {
        tcpAcceptor.shutdown();
    }

    @Override
    public void start() {
        try {
            tcpAcceptor.startup(acceptorPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
