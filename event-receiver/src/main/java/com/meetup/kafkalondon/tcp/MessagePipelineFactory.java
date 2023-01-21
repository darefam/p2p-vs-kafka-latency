package com.meetup.kafkalondon.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class MessagePipelineFactory implements PipelineFactory {
    final MessageDecoder decoder = new MessageDecoder();
    final MessageHandler handler = new MessageHandler();
    private final int availableProcessors;
    private final EventExecutorGroup executors;

    public MessagePipelineFactory() {
        availableProcessors = Runtime.getRuntime().availableProcessors();
        executors = new DefaultEventExecutorGroup(availableProcessors);
    }

    @Override
    public ChannelInitializer<SocketChannel> createInitializer() {

        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", decoder);
                pipeline.addLast(executors, "handler", handler);
            }
        };
    }

    public MessageHandler getMessageHandler() {
        return handler;
    }
}
