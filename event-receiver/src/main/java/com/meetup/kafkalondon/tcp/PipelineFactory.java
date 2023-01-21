package com.meetup.kafkalondon.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public interface PipelineFactory {

    ChannelInitializer<SocketChannel> createInitializer();
}
